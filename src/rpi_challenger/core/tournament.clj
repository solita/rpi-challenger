(ns rpi-challenger.core.tournament
  (:use [clojure.algo.generic.functor :only [fmap]])
  (:require [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.challenges :as c]
            [rpi-challenger.core.rating :as rating]))

(defn make-tournament
  []
  {:participants {}
   :challenge-functions []})

(defn participants
  [tournament]
  (vals (:participants tournament)))

(defn participant-by-id
  [tournament id]
  (get (:participants tournament) id))

(defn register-participant
  [tournament participant]
  (assoc-in tournament [:participants (:id participant)] participant))

(defn record-strike
  [tournament participant strike]
  (update-in tournament [:participants (:id participant)] p/record-strike strike))

(defn update-challenge-functions
  [tournament]
  (assoc-in tournament [:challenge-functions ] (sort-by c/points < (c/find-challenge-functions))))

(defn generate-challenges
  [tournament]
  (map c/generate (:challenge-functions tournament)))

(defn finish-current-round
  [tournament]
  (update-in tournament [:participants ] #(fmap rating/score-current-round %)))
