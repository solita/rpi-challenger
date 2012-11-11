(ns rpi-challenger.core
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.response :as response])
  (:use [net.cgrand.moustache :only [app]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.reload :only [wrap-reload]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]))

(def layout (html/html-resource "resources/layout.html"))

(def routes
  (app
    [""] (fn [req] (response/response "the index page"))
    [&] (fn [req] (response/not-found "Page Not Found"))))
