(ns rpi-challenger.core.rating
  (:require [clojure.string :as string]))

(defn ^:dynamic correct?
  [response challenge]
  (and
    (nil? (:error response))
    (= 200 (:code (:status response)))
    (= (string/trim-newline (:body response)) (:answer challenge))))

; TODO: score based on challenge level, then reduce using `min`
(defn score-strike
  [strike]
  (if (correct? (:response strike) (:challenge strike))
    1
    0))

(defn ^:dynamic score-strikes
  [strikes]
  (apply + (map score-strike strikes)))

(defn score-current-round
  [participant]
  (-> participant
    (update-in [:score ] #(+ % (score-strikes (:recent-strikes participant))))
    (assoc :current-round [])))
