(ns rpi-challenger.app
  (:require [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as strike]
            [rpi-challenger.core.challenges :as c]
            [rpi-challenger.http :as http]
            [rpi-challenger.util.io :as io]
            [rpi-challenger.util.threads :as threads]
            [clojure.string :as string])
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

(defn- with-file-ext [file ext]
  (File. (.getParentFile file) (str (.getName file) "." ext)))

(defn save-state [app file]
  (let [tmp-file (with-file-ext file "tmp")
        bak-file (with-file-ext file "bak")]
    (io/object-to-file tmp-file (t/serialize (:tournament @app)))
    (.delete bak-file)
    (.renameTo file bak-file)
    (.renameTo tmp-file file)))

(defn load-state [file]
  (let [app (make-app)
        tournament (t/deserialize (io/file-to-object file))]
    (dosync (alter-tournament app (fn [_] tournament)))
    app))


; participants

(defn ^:dynamic record-strike [app participant strike]
  (dosync
    (alter-tournament app t/record-strike participant strike)))

(defn poll-participant [app participant challenges]
  (if (not (empty? challenges))
    (let [challenge (first challenges)
          response (http/post-request (:url participant) (c/format-question challenge))
          strike (strike/make-strike response challenge)]
      (record-strike app participant strike)

      ; avoid 100% CPU usage if the URL is invalid (e.g. no such host)
      (if (strike/error? strike)
        (Thread/sleep 1000))

      ; reduce CPU usage also under normal circumstances
      (Thread/sleep 10)

      ; stop on first failed challenge (to keep the harder challenges secret)
      (if (strike/hit? strike)
        (poll-participant app participant (rest challenges))))))

(defn ^:dynamic poll-participant-loop [app participant]
  (while true
    (try
      (poll-participant app participant (t/generate-challenges (:tournament @app)))
      (catch Throwable e
        (.error logger (str "Unhandled error in polling participant " (:url participant)) e)))))

(defn ^:dynamic start-polling [app participant]
  (threads/execute (:thread-pool @app) #(poll-participant-loop app participant)))

(defn get-participants [app]
  (t/participants (:tournament @app)))

(defn get-participant-by-id [app id]
  (t/participant-by-id (:tournament @app) id))

(defn ^:dynamic register-participant [app name url]
  (.info logger "Registering participant \"{}\" at {}" name url)
  (start-polling app (dosync
                       (let [participant (t/make-participant (:tournament @app) name url)]
                         (alter-tournament app t/register-participant participant)
                         participant))))

(defn get-participant-score-history [app id]
  (let [participant (get-participant-by-id app id)
        rounds (p/finished-rounds participant)]
    (map (fn [round] [(:finished round) (:points round) (:max-points round)]) rounds)))


; lifecycle events

(defn ^:dynamic start-new-round [app]
  (.info logger "Saving application state")
  (save-state app app-state-file)

  (.info logger "Starting a new round")
  (dosync (alter-tournament app t/finish-current-round))
  (c/load-challenge-functions challenge-functions-dir)
  (let [fns (c/find-challenge-functions)]
    (dosync (alter-tournament app t/set-challenge-functions fns)))

  (.info logger "Using challenges:\n{}"
    (string/join "\n"
      (map (fn [challenge-fn] (str "\t" (c/price challenge-fn) "\t" challenge-fn))
        (:challenge-functions (:tournament @app))))))

(defn start [app]
  (threads/schedule-repeatedly (:scheduler @app) #(start-new-round app) round-duration-in-seconds)
  (doseq [participant (get-participants app)]
    (start-polling app participant))
  app)
