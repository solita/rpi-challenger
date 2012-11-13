(ns rpi-challenger.routes
  (:use ring.util.response
        compojure.core
        [ring.middleware.params :only [wrap-params]])
  (:require [rpi-challenger.core :as core]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]))

(defn using-template
  [template & args]
  (response (apply str (apply template args))))

(html/deftemplate layout "public/layout.html"
  [{:keys [title main]}]
  [:title ] (html/content title)
  [:#main ] (html/substitute main))

(html/defsnippet overview "public/overview.html" [:body ]
  [{:keys []}])

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
  (GET "/" [] (using-template layout {:title "Overview"
                                      :main (overview {})}))
  (POST "/register" {params :params} (handle-register-form params))
  (route/resources "/")
  (route/not-found "Page Not Found"))

(def app (-> app-routes
           (wrap-params)))
