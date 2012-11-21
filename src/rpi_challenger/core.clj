(ns rpi-challenger.core
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [http.async.client :as http]
            [rpi-challenger.challenges :as challenges]
            [rpi-challenger.rating :as rating]
            [rpi-challenger.io :as io]))

;(defonce thread-pool (java.util.concurrent.Executors/newCachedThreadPool))

(defonce participants (ref {}))

(defn- add-participant
  [participants name url]
  (assoc participants url {:name name, :url url, :score 0, :current-round []}))

(defn register
  [name url]
  (dosync
    (alter participants add-participant name url)))

(defn get-participants
  []
  (vals (deref participants)))

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
    (alter participants update-in [url :current-round ] #(conj % {:timestamp (System/currentTimeMillis),
                                                                  :response response,
                                                                  :challenge challenge})))
  ; TODO: load state on restart
  ; TODO: save state less often
  (io/object-to-file "rpi-challenger-state.clj" (deref participants)))

(defn poll-participant
  [participant]
  (println "Polling" participant)
  (with-open [client (http/create-client)]
    (let [challenge (challenges/hello-world)
          response (post-request (:url participant) (:question challenge))]
      (record-reponse (:url participant) response challenge))))

(defn poll-participants
  []
  (doseq [participant (get-participants)]
    (poll-participant participant)))

(defn calculate-score
  []
  (dosync
    (alter participants #(fmap rating/score-current-round %))))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
