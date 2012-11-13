(ns rpi-challenger.core
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [net.cgrand.enlive-html :as html]))

(html/deftemplate layout "public/layout.html"
  [{:keys [title main]}]
  [:title ] (html/content title)
  [:#main ] (html/substitute main))

(html/defsnippet overview "public/overview.html" [:body ]
  [{:keys []}])

(defn using-template [template & args]
  (response (apply str (apply template args))))

(defroutes app
  (GET "/" [] (using-template layout {:title "Overview"
                                      :main (overview {})}))
  (POST "/register" [] "TODO: handle registration form")
  (route/resources "/")
  (route/not-found "Page Not Found"))
