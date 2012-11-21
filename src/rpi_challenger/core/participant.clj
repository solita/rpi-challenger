(ns rpi-challenger.core.participant
  (:require [rpi-challenger.core.strike :as s]))

(defn make-participant
  [name url]
  {:name name, :url url, :score 0, :current-round []})

(defn strikes
  [participant]
  (:current-round participant))

(defn record-strike
  [participant strike]
  (update-in participant [:current-round ] #(conj % strike)))
