(ns rpi-challenger.core.round
  (:require [rpi-challenger.core.strike :as strike]))

(defn- reduce-or-nil [f coll]
  (if (empty? coll)
    nil
    (reduce f coll)))


(defn- lower-price [a b]
  (if (< (strike/price a) (strike/price b))
    a
    b))

(defn- higher-price [a b]
  (if (> (strike/price a) (strike/price b))
    a
    b))

(defn- price-lower-than? [price]
  (fn [this] (< (strike/price this) price)))


(defn- hits [round] (vals (:hits-by-price round)))
(defn- failures [round] (vals (:failures-by-price round)))
(defn- has-failures? [round] (not (empty? (failures round))))
(defn- error [round] (:error round))
(defn- has-error? [round] (not (nil? (error round))))


(defn- worst-failure [round]
  (or
    (error round)
    (reduce-or-nil lower-price (failures round))))

(defn- accepted-hits-price-limit [round]
  (cond
    (has-error? round) 0
    (has-failures? round) (strike/price (worst-failure round))
    :else Integer/MAX_VALUE))

(defn- significant-hit [round]
  (let [price-limit (accepted-hits-price-limit round)
        accepted-hits (filter (price-lower-than? price-limit) (hits round))]
    (reduce-or-nil higher-price accepted-hits)))

(defn- points [round]
  (or
    (strike/price (significant-hit round))
    0))


(defn start []
  ; We don't need to remember every strike, only one strike for each different price.
  {:started (System/currentTimeMillis)
   :hits-by-price {}
   :failures-by-price {}
   :error nil})

(defn record-strike [round strike]
  (cond
    (strike/error? strike) (assoc-in round [:error ] strike)
    (strike/miss? strike) (assoc-in round [:failures-by-price (strike/price strike)] strike)
    (strike/hit? strike) (assoc-in round [:hits-by-price (strike/price strike)] strike)))

(defn ^:dynamic finish [round]
  {:started (:started round)
   :finished (System/currentTimeMillis)
   :points (points round)
   :significant-hit (significant-hit round)
   :worst-failure (worst-failure round)})


; acceleration-based scoring

(def ^:dynamic acceleration 1)

(defn- points-based-on-acceleration [previous-round round]
  (let [max-points (:points round)
        previous-points (:points previous-round 0)]
    (assoc round
      :max-points max-points
      :points (min max-points (+ acceleration previous-points)))))

(defn apply-point-acceleration [rounds]
  (rest (reductions points-based-on-acceleration nil rounds)))
