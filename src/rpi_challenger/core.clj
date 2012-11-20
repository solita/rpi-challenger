(ns rpi-challenger.core
  (:require [http.async.client :as http]
            [rpi-challenger.challenges :as challenges]
            [rpi-challenger.io :as io]))

;(defonce thread-pool (java.util.concurrent.Executors/newCachedThreadPool))

(defonce services (ref {}))

(defn- add-service
  [services name url]
  (assoc services url {:name name, :url url, :score 0, :responses []}))

(defn register
  [name url]
  (dosync
    (alter services add-service name url)))

(defn get-services
  []
  (vals (deref services)))

(defn nil-or-str
  [object]
  (if (nil? object)
    nil
    (str object)))

(defn simple-http-response
  [response]
  {:body (http/string response)
   :status (http/status response)
   :error (nil-or-str (http/error response))})

(defn post-request
  [url body]
  (with-open [client (http/create-client)]
    (-> (http/POST client url :body body)
      http/await
      simple-http-response)))

(defn correct?
  [response challenge]
  (and
    (nil? (:error response))
    (= 200 (:code (:status response)))
    (= (:body response) (:answer challenge))))

(defn record-reponse
  [url response challenge]
  (println "Record response:" url response challenge)
  (dosync
    (alter services update-in [url :responses ] #(conj % {:timestamp (System/currentTimeMillis),
                                                          :response response,
                                                          :challenge challenge}))
    (if (correct? response challenge)
      (alter services update-in [url :score ] #(+ % 1))
      (alter services update-in [url :score ] #(- % 1))))
  ; TODO: load state on restart
  ; TODO: save state less often
  (io/object-to-file "rpi-challenger-state.clj" (deref services)))


(defn poll-service
  [service]
  (println "Polling" service)
  (with-open [client (http/create-client)]
    (let [challenge (challenges/hello-world)
          response (post-request (:url service) (:challenge challenge))]
      (record-reponse (:url service) response challenge))))

(defn poll-services
  []
  (doseq [service (get-services)]
    (poll-service service)))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
