(ns rpi-challenger.core
  (:require [net.cgrand.enlive-html :as html])
  (:use ring.util.response
        [net.cgrand.moustache :only [app]]))

(def layout (html/html-resource "resources/layout.html"))

(def routes
  (app
    [""] (fn [req] (->
                     (response "the index page")
                     (content-type "text/html")
                     (charset "UTF-8")))
    [&] (fn [req] (->
                    (not-found "Page Not Found")
                    (content-type "text/html")
                    (charset "UTF-8")))))
