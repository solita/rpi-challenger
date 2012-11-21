(ns rpi-challenger.core.tournament
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [rpi-challenger.rating :as rating]))

(defn make-tournament
  []
  {:participants {}})

(defn participants
  [tournament]
  (vals (:participants tournament)))

(defn register-participant
  [tournament {:keys [name url] :as participant}]
  (assoc-in tournament [:participants url] {:name name, :url url, :score 0, :current-round []}))

(defn strikes
  [tournament participant]
  (get-in tournament [:participants (:url participant) :current-round ]))

(defn record-strike
  [tournament participant response challenge]
  (update-in
    tournament
    [:participants (:url participant) :current-round ]
    #(conj % {:timestamp (System/currentTimeMillis),
              :response response,
              :challenge challenge})))

(defn finish-current-round
  [tournament]
  (update-in tournament [:participants ] #(fmap rating/score-current-round %)))
