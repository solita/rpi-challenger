(ns rpi-challenger.core.participant
  (:require [rpi-challenger.core.strike :as strike]
            [rpi-challenger.core.round :as round])
  (:import [java.util.concurrent.atomic AtomicInteger]))

(def ^:dynamic *recent-strikes-limit* 50)
(def ^:dynamic *recent-failures-limit* 10)

(defn make-participant
  [id name url]
  {:id id
   :name name
   :url url
   :score 0
   :current-round (round/start)
   :finished-rounds []
   :recent-strikes []
   :recent-failures []})

(defn finished-rounds [participant] (:finished-rounds participant))
(defn recent-strikes [participant] (:recent-strikes participant))
(defn recent-failures [participant] (:recent-failures participant))

(defn record-strike
  [participant strike]
  (-> participant
    (update-in [:current-round ] #(round/record-strike % strike))
    (update-in [:recent-strikes ] #(take-last *recent-strikes-limit* (concat % [strike])))
    (update-in [:recent-failures ] #(take-last *recent-failures-limit* (concat % (filter strike/miss? [strike]))))))

(defn recalculate-total-score
  [participant]
  (assoc participant :score (reduce + (map :points (finished-rounds participant)))))

(defn finish-current-round
  [participant]
  (let [finished-round (round/finish (:current-round participant))]
    (-> participant
      (update-in [:current-round ] (fn [_] (round/start)))
      (update-in [:finished-rounds ] #(conj % finished-round))
      (recalculate-total-score))))
