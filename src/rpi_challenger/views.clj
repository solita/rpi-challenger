(ns rpi-challenger.views
  (:use net.cgrand.enlive-html))

; layout

(deftemplate layout "public/layout.html"
  [{:keys [title main]}]
  [:title ] (content title)
  [:#main ] (substitute main))


; overview

(def *participants-row [:#participants [:tr (nth-of-type 2)]])
(def *participant-link [[:a (nth-of-type 1)]])
(def *participant-score [[:td (nth-of-type 2)]])

(defsnippet participants-row "public/overview.html" *participants-row
  [{:keys [name url score]}]
  *participant-link (do->
                      (content name)
                      (set-attr :href url))
  *participant-score (content (str score)))

(defsnippet participants-list "public/overview.html" [:body ]
  [participants]
  *participants-row (content (map #(participants-row %) participants)))


; pages

(defn overview-page
  [participants]
  (layout {:title "Overview"
           :main (participants-list participants)}))
