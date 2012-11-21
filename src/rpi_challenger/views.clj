(ns rpi-challenger.views
  (:use net.cgrand.enlive-html)
  (:require [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [clj-time.core :as time])
  (:import [org.joda.time LocalDateTime]))

; layout

(deftemplate layout "public/layout.html"
  [{:keys [title main]}]
  [:title ] (content title)
  [:#main ] (substitute main))


; participants list

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


; strikes list

(defn format-timestamp
  [{timestamp :timestamp}]
  (str (LocalDateTime. timestamp)))

(defn format-status-or-error
  [{{status :status, error :error} :response}]
  (if (nil? error)
    (str (:code status) " " (:msg status))
    (str error)))

(defn strikes-row-class
  [strike]
  (cond
    (s/error? strike) "error"
    (s/miss? strike) "miss"
    (s/hit? strike) "hit"))

(defsnippet strikes-row "public/strikes-list.html" [:table [:tr (nth-of-type 2)]]
  [strike]
  [:tr ] (set-attr :class (strikes-row-class strike))
  [[:td (nth-of-type 1)]] (content (format-timestamp strike))
  [[:td (nth-of-type 2)]] (content (:question (:challenge strike)))
  [[:td (nth-of-type 3)]] (content (:answer (:challenge strike)))
  [[:td (nth-of-type 4)]] (content (:body (:response strike)))
  [[:td (nth-of-type 5)]] (content (format-status-or-error strike)))

(defsnippet strikes-list "public/strikes-list.html" [:table ]
  [strikes]
  [[:tr (nth-of-type 4)]] (substitute)
  [[:tr (nth-of-type 3)]] (substitute)
  [[:tr (nth-of-type 2)]] (substitute (map #(strikes-row %) strikes)))

; pages

(defn overview-page
  [participants]
  (layout {:title "Overview"
           :main (participants-list participants)}))

(defn detail-page
  [participant]
  (layout {:title "Detail"
           :main (strikes-list (reverse (p/strikes participant)))}))

(defn recent-failures-page
  [participant]
  (layout {:title "Recent Failures"
           :main (strikes-list (reverse (p/recent-failures participant)))}))
