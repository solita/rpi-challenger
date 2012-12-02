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


(defn- failures [round] (vals (:failures-by-points round)))

(defn- hits [round] (vals (:hits-by-points round)))


(defn- worst-failure [round]
  (let [failures (failures round)]
    (reduce-or-nil lower-points failures)))

(defn- significant-hit [round]
  (let [worst-failure (worst-failure round)
        accepted-hits (filter (points-lower-than? worst-failure) (hits round))]
    (reduce-or-nil higher-points accepted-hits)))

(defn- points [round]
  (or
    (strike/points (significant-hit round))
    0))


(defn start []
  ; We don't need to remember every strike. Only one strike per unique number of points.
  {:hits-by-points {}
   :failures-by-points {}})

(defn record-strike [round strike]
  (cond
    (strike/hit? strike) (assoc-in round [:hits-by-points (strike/points strike)] strike)
    (strike/miss? strike) (assoc-in round [:failures-by-points (strike/points strike)] strike)))

(defn ^:dynamic finish [round]
  {:points (points round)
   :significant-hit (significant-hit round)
   :worst-failure (worst-failure round)})
