(ns rpi-challenger.core
  (:use ring.util.response
        net.cgrand.enlive-html
        compojure.core)
  (:require [compojure.route :as route]))

(deftemplate layout "public/layout.html" [body]
  [:#content ] (content body))

(defn using-template [template & args]
  (response (apply str (apply template args))))

(defroutes app
  (GET "/" [] (using-template layout "Hello world!"))
  (route/resources "/")
  (route/not-found "Page Not Found"))
