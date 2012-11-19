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

(defn post-request
  [url body]
  (with-open [client (http/create-client)]
    (-> (http/POST client url :body body)
      http/await)))

(defn record-reponse
  [url challenge response-body]
  (println "Record response:" url challenge response-body)
  (if (= (:answer challenge) response-body)
    (dosync
      (alter services update-in [url :score ] #(+ % 1)))
    (dosync
      (alter services update-in [url :score ] #(- % 1)))))

(defn poll-service
  [service]
  (println "Polling" service)
  (with-open [client (http/create-client)]
    (let [challenge (challenges/hello-world)
          response (post-request (:url service) (:challenge challenge))]
      (println "status" (http/status response))
      (println "error" (http/error response))
      (println "body" (http/string response))
      (record-reponse (:url service) challenge (http/string response)))))

(defn poll-services
  []
  (doseq [service (get-services)]
    (poll-service service)))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
