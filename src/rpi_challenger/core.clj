(ns rpi-challenger.core
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [http.async.client :as http]
            [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.challenges :as challenges]
            [rpi-challenger.rating :as rating]
            [rpi-challenger.io :as io]))

;(defonce thread-pool (java.util.concurrent.Executors/newCachedThreadPool))

(defonce tournament (ref (t/make-tournament)))

(defn register
  [name url]
  (dosync
    (alter tournament t/register-participant (p/make-participant name url))))

(defn get-participants
  []
  (t/participants (deref tournament)))

(defn get-participant-by-id
  [id]
  (t/participant-by-id (deref tournament) id))

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
  [participant response challenge]
  (println "Record strike:" participant response challenge)
  (dosync
    (alter tournament t/record-strike participant (s/make-strike response challenge)))
  ; TODO: load state on restart
  ; TODO: save state less often
  (io/object-to-file "rpi-challenger-state.clj" (deref tournament)))

(defn poll-participant
  [participant]
  (with-open [client (http/create-client)]
    (let [challenge (challenges/hello-world)
          response (post-request (:url participant) (:question challenge))]
      (record-reponse participant response challenge))))

(defn poll-participants
  []
  (doseq [participant (get-participants)]
    (poll-participant participant)))

(defn calculate-score
  []
  (dosync
    (alter tournament t/finish-current-round)))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
