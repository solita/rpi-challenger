(ns rpi-challenger.routes
  (:use ring.util.response
        compojure.core
        [ring.middleware.params :only [wrap-params]])
  (:require [rpi-challenger.app :as app]
            [rpi-challenger.views :as views]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [clj-json.core :as json]))

(defn json-response
  [data]
  (-> (response (json/generate-string data))
    (content-type "application/json")))

(defn using-template
  [template & args]
  (-> (response (apply str (apply template args)))
    (content-type "text/html")
    (charset "UTF-8")))

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

(defn handle-overall-scores
  [app]
  (map (fn [x] (select-keys x [:name :score ])) (app/get-participants app)))

(defn make-routes
  [app]
  (->
    (routes
      (GET "/" [] (using-template views/tournament-overview-page (app/get-participants app)))
      (GET "/participant-:id/score-history" [id] (json-response (app/get-participant-score-history app (Integer/parseInt id))))
      (GET "/participant-:id" [id] (using-template views/participant-details-page (app/get-participant-by-id app (Integer/parseInt id))))
      (POST "/register" {params :params} (handle-register-form app params))
      (POST "/hello-world" {body :body} (handle-hello-world (slurp body)))
      (GET "/overall-scores" [] (json-response (handle-overall-scores app)))
      (GET "/overall" [] (using-template views/overall-scores-page))
      (route/resources "/")
      (route/not-found "404 Page Not Found"))
    (wrap-params)))
