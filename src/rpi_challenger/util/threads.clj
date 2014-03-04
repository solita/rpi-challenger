(ns rpi-challenger.util.threads
  (:import [com.google.common.util.concurrent ThreadFactoryBuilder]
           [java.util.concurrent ExecutorService ScheduledExecutorService TimeUnit]
           [org.slf4j LoggerFactory]))

(def ^:dynamic logger (LoggerFactory/getLogger (str (ns-name *ns*))))

(defn daemon-thread-factory [name-prefix]
  (-> (ThreadFactoryBuilder.)
      (.setNameFormat (str name-prefix "-%d"))
      (.setDaemon true)
      (.build)))

(defn with-error-logging [f]
  #(try
    (f)
    (catch Exception e
      (.error logger "Uncaught exception" e))))

(defn ^:dynamic schedule-repeatedly [^ScheduledExecutorService scheduler f interval-in-seconds]
  (.scheduleAtFixedRate scheduler (with-error-logging f) 0 interval-in-seconds TimeUnit/SECONDS))

(defn ^:dynamic submit [^ExecutorService executor f]
  (.submit executor (with-error-logging f)))
