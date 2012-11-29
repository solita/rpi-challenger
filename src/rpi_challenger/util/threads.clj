(ns rpi-challenger.util.threads
  (:import [com.google.common.util.concurrent ThreadFactoryBuilder]
           [java.util.concurrent ScheduledExecutorService TimeUnit]))

(defn daemon-thread-factory [name-prefix]
  (-> (ThreadFactoryBuilder.)
    (.setNameFormat (str name-prefix "-%d"))
    (.setDaemon true)
    (.build)))

(defn ^:dynamic schedule-with-fixed-delay [^ScheduledExecutorService scheduler f delay-seconds]
  (.scheduleWithFixedDelay scheduler f 0 delay-seconds TimeUnit/SECONDS))
