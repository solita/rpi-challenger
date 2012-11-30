(ns rpi-challenger.routes
  (:use ring.util.response
        compojure.core
        [ring.middleware.params :only [wrap-params]])
  (:require [rpi-challenger.app :as app]
            [rpi-challenger.views :as views]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]))

(defn using-template
  [template & args]
  (response (apply str (apply template args))))

(defn handle-register-form
  [app {name "name" url "url"}]
  (if (and
        (not (string/blank? name))
        (not (string/blank? url)))
    (do
      (app/register-participant app name url)
      (redirect "/"))
    (redirect "/?message=Registration failed")))

(defn handle-hello-world
  [question]
  (let [[op & args] (string/split-lines question)]
    (cond
      (= op "ping") "pong"
      (= op "say-hello") (str "Hello " (first args))
      :else (str "Don't understand:\n" question))))

(defn make-routes
  [app]
  (->
    (routes
      (GET "/" [] (using-template views/tournament-overview-page (app/get-participants app)))
      (GET "/participant-:id" [id] (using-template views/participant-details-page (app/get-participant-by-id app (Integer/parseInt id))))
      (POST "/register" {params :params} (handle-register-form app params))
      (POST "/hello-world" {body :body} (handle-hello-world (slurp body)))
      (route/resources "/")
      (route/not-found "Page Not Found"))
    (wrap-params)))
