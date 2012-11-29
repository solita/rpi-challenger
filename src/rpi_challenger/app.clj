(ns rpi-challenger.app
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
           [java.util.concurrent Executors Future ScheduledExecutorService TimeUnit]
           [com.google.common.util.concurrent ThreadFactoryBuilder]))

(defn daemon-thread-factory [name-prefix]
  (-> (ThreadFactoryBuilder.)
    (.setNameFormat (str name-prefix "-%d"))
    (.setDaemon true)
    (.build)))


(defn make-app []
  (ref {:tournament (t/make-tournament)
        :scheduler (Executors/newScheduledThreadPool 1 (daemon-thread-factory "round-scheduler"))
        :thread-pool (Executors/newCachedThreadPool (daemon-thread-factory "participant-poller"))
        :participant-pollers []}))
