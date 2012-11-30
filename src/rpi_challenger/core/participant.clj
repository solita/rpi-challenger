(ns rpi-challenger.core.participant
  (:require [rpi-challenger.core.strike :as s])
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
    (update-in [:recent-failures ] #(take-last *recent-failures-limit* (concat % (filter s/miss? [strike]))))))

(defn recent-strikes
  [participant]
  (:recent-strikes participant))

(defn recent-failures
  [participant]
  (:recent-failures participant))
