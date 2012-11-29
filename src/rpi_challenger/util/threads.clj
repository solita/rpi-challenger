(ns rpi-challenger.util.threads
  (:import [com.google.common.util.concurrent ThreadFactoryBuilder]))

(defn daemon-thread-factory [name-prefix]
  (-> (ThreadFactoryBuilder.)
    (.setNameFormat (str name-prefix "-%d"))
    (.setDaemon true)
    (.build)))
