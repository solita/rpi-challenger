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

(defn make-app []
  (ref {:tournament (t/make-tournament)
        :scheduler (Executors/newScheduledThreadPool 1 (threads/daemon-thread-factory "round-scheduler"))
        :thread-pool (Executors/newCachedThreadPool (threads/daemon-thread-factory "participant-poller"))
        :participant-pollers []}))

(defn- alter-tournament [app f & args]
  (apply alter (concat [app update-in [:tournament ] f] args)))


; participants

(defn register-participant [app name url]
  (.info logger "Registering participant \"{}\" at {}" name url)
  (let [participant (p/make-participant name url)]
    (dosync
      (alter-tournament app t/register-participant participant))
    (:id participant)))

(defn get-participants [app]
  (t/participants (:tournament @app)))

(defn get-participant-by-id [app id]
  (t/participant-by-id (:tournament @app) id))


; lifecycle events

(defn ^:dynamic start-new-round [app])

(defn start [app]
  (threads/schedule-with-fixed-delay (:scheduler @app) #(start-new-round app) round-duration-in-seconds))
