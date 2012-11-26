(ns rpi-challenger.core
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [http.async.client :as http]
            [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.challenges :as c]
            [rpi-challenger.rating :as rating]
            [rpi-challenger.io :as io])
  (:import [org.slf4j LoggerFactory]))

;(defonce thread-pool (java.util.concurrent.Executors/newCachedThreadPool))

(def ^:dynamic logger (LoggerFactory/getLogger (str (ns-name *ns*))))

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

(defn ^:dynamic post-request
  [url body]
  (try
    (with-open [client (http/create-client)]
      (-> (http/POST client url :body body :timeout 1000)
        http/await
        simple-http-response))
    (catch Throwable t
      (.warn logger (str "Failed to POST to " url) t)
      {:body nil
       :status nil
       :error (.toString t)})))

(defn ^:dynamic record-response
  [participant response challenge]
  (dosync
    (alter tournament t/record-strike participant (s/make-strike response challenge)))
  ; TODO: load state on restart
  ; TODO: save state less often
  (io/object-to-file "rpi-challenger-state.clj" (deref tournament)))

(defn poll-participant
  [participant challenges]
  (doseq [challenge challenges]
    (let [response (post-request (:url participant) (:question challenge))]
      (record-response participant response challenge))))

(defn poll-participants
  []
  (c/load-challenge-functions)
  (let [challenges (c/generate-challenges)]
    (doseq [participant (get-participants)]
      (poll-participant participant challenges))))

(defn calculate-score
  []
  (dosync
    (alter tournament t/finish-current-round)))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
