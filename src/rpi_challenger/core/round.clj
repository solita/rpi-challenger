(ns rpi-challenger.core.round
  (:require [rpi-challenger.core.strike :as strike]))

(defn- reduce-or-nil [f coll]
  (if (empty? coll)
    nil
    (reduce f coll)))


(defn- lower-points [a b]
  (if (< (strike/points a) (strike/points b))
    a
    b))

(defn- higher-points [a b]
  (if (> (strike/points a) (strike/points b))
    a
    b))

(defn- points-lower-than? [that]
  (if (nil? that)
    (fn [this] true)
    (fn [this] (< (strike/points this) (strike/points that)))))


(defn- worst-failure [strikes]
  (let [failures (filter strike/miss? strikes)]
    (reduce-or-nil lower-points failures)))

(defn- significant-hit [strikes]
  (let [worst-failure (worst-failure strikes)
        all-hits (filter strike/hit? strikes)
        accepted-hits (filter (points-lower-than? worst-failure) all-hits)]
    (reduce-or-nil higher-points accepted-hits)))

(defn- points [strikes]
  (or
    (strike/points (significant-hit strikes))
    0))


(defn start []
  [])

(defn record-strike [round strike]
  (conj round strike))

(defn ^:dynamic finish [round]
  {:points (points round)
   :significant-hit (significant-hit round)
   :worst-failure (worst-failure round)})
