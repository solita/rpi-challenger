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

(defn handle-hello-world
  [challenge]
  (if (= challenge "Say hello to World")
    "Hello World"
    (str "Wut iz '" challenge "'?")))

(defroutes app-routes
  (GET "/" [] (using-template views/overview-page (core/get-services)))
  (POST "/register" {params :params} (handle-register-form params))
  (GET "/poll" [] (str (core/poll-services)))
  (GET "/calculate-score" [] (str (core/calculate-score)))
  (POST "/hello-world" {body :body, :as request}
    ;(println request)
    (handle-hello-world (slurp body)))
  (route/resources "/")
  (route/not-found "Page Not Found"))

(def app (-> app-routes
           (wrap-params)))
