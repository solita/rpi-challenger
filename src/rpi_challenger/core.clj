(ns rpi-challenger.core
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [http.async.client :as http]
            [rpi-challenger.challenges :as challenges]
            [rpi-challenger.rating :as rating]
            [rpi-challenger.io :as io]))

;(defonce thread-pool (java.util.concurrent.Executors/newCachedThreadPool))

(defonce services (ref {}))

(defn- add-service
  [services name url]
  (assoc services url {:name name, :url url, :score 0, :new-events []}))

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

(defn record-reponse
  [url response challenge]
  (println "Record response:" url response challenge)
  (dosync
    (alter services update-in [url :new-events ] #(conj % {:timestamp (System/currentTimeMillis),
                                                           :response response,
                                                           :challenge challenge})))
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

(defn calculate-score
  []
  (dosync
    (alter services #(fmap rating/score-tick %))))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
