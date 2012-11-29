(ns rpi-challenger.controller
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [http.async.client :as http]
            [rpi-challenger.app :as app]
            [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.challenges :as c]
            [rpi-challenger.core.rating :as rating]
            [rpi-challenger.util.io :as io]
            [rpi-challenger.util.threads :as threads])
  (:import [org.slf4j LoggerFactory Logger]
           [java.io File]
           [java.util.concurrent Executors Future ScheduledExecutorService TimeUnit]))

(def tournament-file (File. "rpi-challenger-state.clj"))

(defn save-tournament
  [tournament]
  (io/object-to-file tournament-file (:participants tournament)))

(defn load-tournament
  []
  (if (.exists tournament-file)
    (assoc (t/make-tournament) :participants (io/file-to-object tournament-file))
    (t/make-tournament)))

(defonce thread-pool (Executors/newCachedThreadPool (threads/daemon-thread-factory "participant-poller")))

(defonce participant-pollers (ref []))

(def ^:dynamic logger (LoggerFactory/getLogger (str (ns-name *ns*))))

(defonce tournament (ref (load-tournament)))

(defn get-participants
  [app]
  (t/participants @tournament))

(defn get-participant-by-id
  [app id]
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
    (catch InterruptedException e
      (throw e))
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

(defn start-polling
  [participant]
  (let [future (.submit thread-pool (make-poller participant))]
    (dosync
      (alter participant-pollers conj future))))

(defn ^:dynamic register
  [name url]
  (.info logger "Registering participant \"{}\" at {}" name url)
  (let [participant (p/make-participant name url)]
    (dosync
      (alter tournament t/register-participant participant))))

(defn start-new-round
  []
  (.info logger "Starting a new round")

  (dosync
    (alter tournament t/finish-current-round)
    (doseq [poller @participant-pollers]
      (.cancel poller true)) ; XXX: interrupting the pollers produces randomly exceptions in NIO/Netty; use a better way of starting pollers on load
    (alter participant-pollers empty))
  (save-tournament @tournament)

  (c/load-challenge-functions (File. "../rpi-challenges/src/")) ; TODO: parameterize the dir on command line or create an admin screen
  (dosync
    (alter tournament t/update-challenge-functions))
  (doseq [participant (t/participants @tournament)]
    (start-polling participant)))

(defonce round-scheduler
  (let [scheduler (Executors/newScheduledThreadPool 1 (threads/daemon-thread-factory "round-scheduler"))]
    (.scheduleWithFixedDelay scheduler start-new-round 0 60 TimeUnit/SECONDS)
    scheduler))
