(ns rpi-challenger.core
  (:use [net.cgrand.moustache :only [app]]
        [ring.adapter.jetty :only [run-jetty]])
  (:gen-class ))

(def my-app
  (app
    [""] "welcome"
    ["hi"] "hello world"
    ["ho"] "howdy"))

(defn -main
  [& args]
  (run-jetty my-app {:port 3000}))
