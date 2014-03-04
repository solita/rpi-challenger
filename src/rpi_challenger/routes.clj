(ns rpi-challenger.routes
  (:use ring.util.response
        compojure.core)
  (:require [rpi-challenger.app :as app]
            [rpi-challenger.views :as views]
            [rpi-challenger.http :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
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

(defn- http-server?
  [url]
  (not (http/error? (http/post-request url ""))))

(defn handle-register-form
  [app {:keys [name url]}]
  (cond
    (string/blank? name) (redirect "/?message=Registration failed: name missing")
    (string/blank? url) (redirect "/?message=Registration failed: URL missing")
    (not (http-server? url)) (redirect (str "/?message=Registration failed: no HTTP server at " url))
    :else (do
            (app/register-participant app name url)
            (redirect "/"))))

(defn handle-dummy
  [op question]
  (let [args (string/split-lines question)]
    (cond
      (= op "ping") "pong"
      (= op "say-hello") (str "Hello " (first args))
      :else (route/not-found (str "Unknown challenge: " op)))))

(defn handle-overall-scores
  [app]
  (map (fn [x] (select-keys x [:name :score])) (app/get-participants app)))

(defn make-routes
  [app]
  (->
    (routes
      (GET "/" [message] (using-template views/tournament-overview-page (app/get-participants app) message))
      (GET "/participant-:id/score-history" [id] (json-response (app/get-participant-score-history app (Integer/parseInt id))))
      (GET "/participant-:id" [id] (using-template views/participant-details-page (app/get-participant-by-id app (Integer/parseInt id))))
      (POST "/register" {params :params} (handle-register-form app params))
      (POST "/dummy/:op" {{op :op} :params
                          body     :body} (handle-dummy op (slurp body)))
      (GET "/overall-scores" [] (json-response (handle-overall-scores app)))
      (GET "/overall" [] (using-template views/overall-scores-page))
      (route/resources "/")
      (route/not-found "404 Page Not Found"))
    (handler/site)))
