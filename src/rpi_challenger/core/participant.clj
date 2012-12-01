(ns rpi-challenger.core.participant
  (:require [rpi-challenger.core.strike :as strike]
            [rpi-challenger.core.round :as round]
            [rpi-challenger.core.rating :as rating])
  (:import [java.util.concurrent.atomic AtomicInteger]))

(def ^:dynamic *recent-strikes-limit* 50)
(def ^:dynamic *recent-failures-limit* 10)

(defn make-participant
  [id name url]
  {:id id
   :name name
   :url url
   :score 0
   :recent-strikes []
   :recent-failures []})

(defn record-strike
  [participant strike]
  (-> participant
    (update-in [:recent-strikes ] #(take-last *recent-strikes-limit* (concat % [strike])))
    (update-in [:recent-failures ] #(take-last *recent-failures-limit* (concat % (filter strike/miss? [strike]))))))

(defn recent-strikes
  [participant]
  (:recent-strikes participant))

(defn recent-failures
  [participant]
  (:recent-failures participant))

(defn finish-current-round
  [participant]
  (let [finished-round (round/finish (:recent-strikes participant))]
    (update-in participant [:score ] #(+ % (:points finished-round)))))
