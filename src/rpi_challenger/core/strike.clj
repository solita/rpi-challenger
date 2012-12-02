(ns rpi-challenger.core.strike
  (:require [rpi-challenger.http :as http]
            [clojure.string :as string]))

(defn make-strike
  [response challenge]
  {:timestamp (System/currentTimeMillis),
   :response response,
   :challenge challenge})

(defn correct-response?
  [response challenge]
  (and
    (nil? (:error response))
    (= 200 (:code (:status response)))
    (= (string/trim-newline (:body response)) (:answer challenge))))

(defn ^:dynamic hit?
  [strike]
  (correct-response? (:response strike) (:challenge strike)))

(defn miss?
  [strike]
  (not (hit? strike)))

(defn ^:dynamic error?
  [strike]
  (http/error? (:response strike)))

(defn ^:dynamic price
  [strike]
  (:price (:challenge strike)))
