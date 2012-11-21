(ns rpi-challenger.core.participant
  (:require [rpi-challenger.core.strike :as s]))

(def ^:dynamic *recent-failures-limit* 10)

(defn make-participant
  [name url]
  {:name name, :url url, :score 0, :recent-failures [], :current-round []})

(defn strikes
  [participant]
  (:current-round participant))

(defn record-strike
  [participant strike]
  (-> participant
    (update-in [:current-round ] #(conj % strike))
    (update-in [:recent-failures ] #(take-last *recent-failures-limit* (concat % (filter s/miss? [strike]))))))

(defn recent-failures
  [participant]
  (:recent-failures participant))
