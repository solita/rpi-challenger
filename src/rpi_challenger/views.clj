(ns rpi-challenger.views
  (:use net.cgrand.enlive-html))

; layout

(deftemplate layout "public/layout.html"
  [{:keys [title main]}]
  [:title ] (content title)
  [:#main ] (substitute main))


; overview

(def *services-row [:#services [:tr (nth-of-type 2)]])
(def *service-link [[:a (nth-of-type 1)]])
(def *service-score [[:td (nth-of-type 2)]])

(defsnippet services-row "public/overview.html" *services-row
  [{:keys [name url score]}]
  *service-link (do->
                  (content name)
                  (set-attr :href url))
  *service-score (content (str score)))

(defsnippet services-list "public/overview.html" [:body ]
  [services]
  *services-row (content (map #(services-row %) services)))


; pages

(defn overview-page
  [services]
  (layout {:title "Overview"
           :main (services-list services)}))
