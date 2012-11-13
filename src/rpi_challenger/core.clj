(ns rpi-challenger.core
  (:use ring.util.response
        compojure.core
        [ring.middleware.params :only [wrap-params]])
  (:require [compojure.route :as route]
            [net.cgrand.enlive-html :as html]))

(defn register
  [name url]
  (println "Registering" name "with url" url)) ; TODO
