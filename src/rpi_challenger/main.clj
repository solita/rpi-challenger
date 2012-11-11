(ns rpi-challenger.main
  (:use [rpi-challenger.core :only [app]]
        ring.adapter.jetty
        ring.middleware.file
        ring.middleware.reload
        ring.middleware.stacktrace)
  (:gen-class ))

(def app-auto-reload
  (-> #'app
    (wrap-file "src/resources")
    (wrap-reload '(rpi-challenger.core))
    (wrap-stacktrace)))

(defn boot []
  (run-jetty #'app-auto-reload {:port 80}))

(defn boot-async []
  (.start (Thread. (fn [] (boot)))))

(defn -main [& args]
  (boot-async))
