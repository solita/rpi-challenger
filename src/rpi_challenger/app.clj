(ns rpi-challenger.app
  (:require [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.challenges :as c]
            [rpi-challenger.core.rating :as rating]
            [rpi-challenger.http :as http]
            [rpi-challenger.util.io :as io]
            [rpi-challenger.util.threads :as threads])
  (:import [org.slf4j LoggerFactory Logger]
           [java.io File]
           [java.util.concurrent Executors Future ScheduledExecutorService TimeUnit]))

(def ^:dynamic logger (LoggerFactory/getLogger (str (ns-name *ns*))))

(def round-duration-in-seconds 60)

(def challenge-functions-dir (File. "../rpi-challenges/src/")) ; TODO: parameterize the dir on command line or create an admin screen

(def app-state-file (File. "rpi-challenger-state.clj"))


(defn make-app []
  (ref {:tournament (t/make-tournament)
        :scheduler (Executors/newScheduledThreadPool 1 (threads/daemon-thread-factory "round-scheduler"))
        :thread-pool (Executors/newCachedThreadPool (threads/daemon-thread-factory "participant-poller"))
        :participant-pollers []}))

(defn- alter-tournament [app f & args]
  (apply alter (concat [app update-in [:tournament ] f] args)))


; persistence

(defn save-state [app file]
  (io/object-to-file file (t/serialize (:tournament @app))))

(defn load-state [file]
  (let [app (make-app)
        tournament (t/deserialize (io/file-to-object file))]
    (dosync (alter-tournament app (fn [_] tournament)))
    app))


; participants

(defn ^:dynamic record-response [app participant response challenge]) ; TODO

(defn poll-participant [app participant challenges]
  (if (not (empty? challenges))
    (let [challenge (first challenges)
          response (http/post-request (:url participant) (:question challenge))]
      (record-response app participant response challenge)

      ; stop on first failure
      (if (rating/correct? response challenge)
        (poll-participant app participant (rest challenges))))))

(defn ^:dynamic poll-participant-loop [app participant]
  (while (not (Thread/interrupted))
    (poll-participant app participant (t/generate-challenges (:tournament @app)))))

(defn start-polling [app participant]
  (threads/execute (:thread-pool @app) #(poll-participant-loop app participant)))

(defn register-participant [app name url]
  (.info logger "Registering participant \"{}\" at {}" name url)
  (let [participant (p/make-participant name url)]
    (dosync
      (alter-tournament app t/register-participant participant))
    (start-polling app participant)
    (:id participant)))

(defn get-participants [app]
  (t/participants (:tournament @app)))

(defn get-participant-by-id [app id]
  (t/participant-by-id (:tournament @app) id))


; lifecycle events

(defn ^:dynamic start-new-round [app]
  (dosync (alter-tournament app t/finish-current-round))
  (save-state app app-state-file)
  (c/load-challenge-functions challenge-functions-dir)
  (dosync (alter-tournament app t/update-challenge-functions)))

(defn start [app]
  ; TODO: start participant pollers (if we were just deserialized)
  (threads/schedule-with-fixed-delay (:scheduler @app) #(start-new-round app) round-duration-in-seconds))
