(ns rpi-challenger.main
  (:use [rpi-challenger.routes :only [app]]
        [clojure.tools.cli :only [cli]]
        ring.adapter.jetty
        ring.middleware.file
        ring.middleware.reload
        ring.middleware.stacktrace)
  (:gen-class ))

(def app-auto-reload
  (-> #'app
    (wrap-reload
      '(rpi-challenger.challenges)
      '(rpi-challenger.core)
      '(rpi-challenger.routes)
      '(rpi-challenger.views))
    (wrap-stacktrace)))

(defn run [options]
  (run-jetty #'app-auto-reload options))

(defn start [options]
  (.start (Thread. (fn [] (run options)))))

(defn -main [& args]
  (let [[options args banner] (cli args
    ["--port" "Port for the HTTP server to listen to" :default 8080 :parse-fn #(Integer. %)]
    ["--help" "Show this help" :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (start options)))
