(ns rpi-challenger.core
  (:use ring.util.response
        compojure.core
        [ring.middleware.params :only [wrap-params]])
  (:require [compojure.route :as route]
            [net.cgrand.enlive-html :as html]))

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
  (println "Registering" name "with url" url) ; TODO
  (redirect "/"))

(defroutes app-routes
  (GET "/" [] (using-template layout {:title "Overview"
                                      :main (overview {})}))
  (POST "/register" {params :params} (handle-register-form params))
  (route/resources "/")
  (route/not-found "Page Not Found"))

(def app (-> app-routes
           (wrap-params)))
