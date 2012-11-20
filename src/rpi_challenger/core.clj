(ns rpi-challenger.core
  (:require [http.async.client :as http]
            [rpi-challenger.challenges :as challenges]))

;(defonce thread-pool (java.util.concurrent.Executors/newCachedThreadPool))

(defonce services (ref {}))

(defn- add-service
  [services name url]
  (assoc services url {:name name :url url :score 0}))

(defn register
  [name url]
  (dosync
    (alter services add-service name url)))

(defn get-services
  []
  (vals (deref services)))

(defn simple-http-response
  [response]
  {:body (http/string response)
   :status (http/status response)
   :error (http/error response)})

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
    (if (correct? response challenge)
      (alter services update-in [url :score ] #(+ % 1))
      (alter services update-in [url :score ] #(- % 1)))))

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
