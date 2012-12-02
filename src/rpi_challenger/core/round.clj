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

(defn- points-lower-than? [points]
  (fn [this] (< (strike/points this) points)))


(defn- hits [round] (vals (:hits-by-points round)))
(defn- failures [round] (vals (:failures-by-points round)))
(defn- has-failures? [round] (not (empty? (failures round))))
(defn- error [round] (:error round))
(defn- has-error? [round] (not (nil? (error round))))


(defn- worst-failure [round]
  (or
    (error round)
    (reduce-or-nil lower-points (failures round))))

(defn- accepted-hits-point-limit [round]
  (cond
    (has-error? round) 0
    (has-failures? round) (strike/points (worst-failure round))
    :else Integer/MAX_VALUE))

(defn- significant-hit [round]
  (let [limit (accepted-hits-point-limit round)
        accepted-hits (filter (points-lower-than? limit) (hits round))]
    (reduce-or-nil higher-points accepted-hits)))

(defn- points [round]
  (or
    (strike/points (significant-hit round))
    0))


(defn start []
  ; We don't need to remember every strike. Only one strike per unique number of points.
  {:hits-by-points {}
   :failures-by-points {}
   :error nil})

(defn record-strike [round strike]
  (cond
    (strike/error? strike) (assoc-in round [:error ] strike)
    (strike/miss? strike) (assoc-in round [:failures-by-points (strike/points strike)] strike)
    (strike/hit? strike) (assoc-in round [:hits-by-points (strike/points strike)] strike)))

(defn ^:dynamic finish [round]
  {:points (points round)
   :significant-hit (significant-hit round)
   :worst-failure (worst-failure round)})
