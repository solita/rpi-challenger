(ns rpi-challenger.main
  (:use [rpi-challenger.routes :only [app]]
        ring.adapter.jetty
        ring.middleware.file
        ring.middleware.reload
        ring.middleware.stacktrace)
  (:gen-class ))

(def app-auto-reload
  (-> #'app
    (wrap-reload
      '(rpi-challenger.core)
      '(rpi-challenger.routes)
      '(rpi-challenger.views))
    (wrap-stacktrace)))

(defn boot []
  (run-jetty #'app-auto-reload {:port 80}))

(defn boot-async []
  (.start (Thread. (fn [] (boot)))))

(defn -main [& args]
  (boot-async))
