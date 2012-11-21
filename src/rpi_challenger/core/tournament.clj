(ns rpi-challenger.core.tournament
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.rating :as rating]))

(defn make-tournament
  []
  {:participants {}})

(defn participants
  [tournament]
  (vals (:participants tournament)))

(defn register-participant
  [tournament participant]
  (assoc-in tournament [:participants (:url participant)] participant))

(defn strikes
  [tournament participant]
  (p/strikes (get-in tournament [:participants (:url participant)])))

(defn record-strike
  [tournament participant strike]
  (update-in tournament [:participants (:url participant)] p/record-strike strike))

(defn finish-current-round
  [tournament]
  (update-in tournament [:participants ] #(fmap rating/score-current-round %)))
