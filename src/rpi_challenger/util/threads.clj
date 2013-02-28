(ns rpi-challenger.util.threads
  (:import [com.google.common.util.concurrent ThreadFactoryBuilder]
           [java.util.concurrent ExecutorService ScheduledExecutorService TimeUnit]))

(defn daemon-thread-factory [name-prefix]
  (-> (ThreadFactoryBuilder.)
    (.setNameFormat (str name-prefix "-%d"))
    (.setDaemon true)
    (.build)))

(defn ^:dynamic schedule-repeatedly [^ScheduledExecutorService scheduler f interval-in-seconds]
  (.scheduleAtFixedRate scheduler f 0 interval-in-seconds TimeUnit/SECONDS))

(defn ^:dynamic submit [^ExecutorService executor f]
  (.submit executor f))
