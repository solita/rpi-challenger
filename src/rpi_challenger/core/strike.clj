(ns rpi-challenger.core.strike
  (:require [rpi-challenger.rating :as rating]))

(defn make-strike
  [response challenge]
  {:timestamp (System/currentTimeMillis),
   :response response,
   :challenge challenge})

(defn ^:dynamic hit?
  [strike]
  (rating/correct? (:response strike) (:challenge strike))) ; TODO: inline from rpi-challenger.rating

(defn miss?
  [strike]
  (not (hit? strike)))

(defn error?
  [strike]
  (not (nil? (:error (:response strike)))))
