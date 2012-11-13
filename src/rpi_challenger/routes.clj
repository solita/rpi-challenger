(ns rpi-challenger.routes
  (:use ring.util.response
        compojure.core
        [ring.middleware.params :only [wrap-params]])
  (:require [rpi-challenger.core :as core]
            [rpi-challenger.views :as views]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]))

(defn using-template
  [template & args]
  (response (apply str (apply template args))))

(defn handle-register-form
  [{name "name" url "url"}]
  (if (and
        (not (string/blank? name))
        (not (string/blank? url)))
    (do
      (core/register name url)
      (redirect "/?message=OK"))
    (redirect "/?message=Registration failed")))

(defroutes app-routes
  (GET "/" [] (using-template views/overview (core/get-services)))
  (POST "/register" {params :params} (handle-register-form params))
  (route/resources "/")
  (route/not-found "Page Not Found"))

(def app (-> app-routes
           (wrap-params)))
