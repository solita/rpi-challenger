(ns rpi-challenger.controller
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [http.async.client :as http]
            [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.challenges :as c]
            [rpi-challenger.core.rating :as rating]
            [rpi-challenger.util.io :as io])
  (:import [org.slf4j LoggerFactory Logger]
           [java.io File]
           [java.util.concurrent Executors ScheduledExecutorService TimeUnit]))

(defonce thread-pool (Executors/newCachedThreadPool))

(def ^:dynamic logger (LoggerFactory/getLogger (str (ns-name *ns*))))

(defonce tournament (ref (t/make-tournament)))

(defn get-participants
  []
  (t/participants @tournament))

(defn get-participant-by-id
  [id]
  (t/participant-by-id @tournament id))

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
    (alter tournament t/record-strike participant (s/make-strike response challenge))))

(defn poll-participant
  [participant challenges]
  (doseq [challenge challenges]
    (let [response (post-request (:url participant) (:question challenge))]
      (record-response participant response challenge))))

(defn make-poller
  [participant]
  #(while (not (Thread/interrupted))
     (poll-participant participant (t/generate-challenges @tournament))))

(defn ^:dynamic register
  [name url]
  (.info logger "Registering participant \"{}\" at {}" name url)
  (let [participant (p/make-participant name url)]
    (dosync
      (alter tournament t/register-participant participant))
    (.execute thread-pool (make-poller participant))))

(defn start-new-round
  []
  (dosync (alter tournament t/finish-current-round))
  (.info logger "Starting a new round")
  ; TODO: load state on restart
  (io/object-to-file "rpi-challenger-state.clj" @tournament)
  ; TODO: parameterize the dir on command line or create an admin screen
  (c/load-challenge-functions (File. "../rpi-challenges/src/"))
  (dosync (alter tournament t/update-challenge-functions)))

(defonce round-scheduler
  (let [scheduler (Executors/newScheduledThreadPool 1)]
    (.scheduleAtFixedRate scheduler start-new-round 0 60 TimeUnit/SECONDS)
    scheduler))

; TODO: remove this dummy data
(register "Hello World Dummy", "http://localhost:8080/hello-world")
