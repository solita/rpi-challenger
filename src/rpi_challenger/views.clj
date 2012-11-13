(ns rpi-challenger.views
  (:require [net.cgrand.enlive-html :as html]))

(html/deftemplate layout "public/layout.html"
  [{:keys [title main]}]
  [:title ] (html/content title)
  [:#main ] (html/substitute main))

(html/defsnippet overview-body "public/overview.html" [:body ]
  [{:keys []}])

(defn overview
  [services]
  (println "Services: " services)
  (layout {:title "Overview"
           :main (overview-body {})}))
