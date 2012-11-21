(ns rpi-challenger.rating)

(defn ^:dynamic correct?
  [response challenge]
  (and
    (nil? (:error response))
    (= 200 (:code (:status response)))
    (= (:body response) (:answer challenge))))

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
    (update-in [:score ] #(+ % (score-strikes (:current-round participant))))
    (assoc :current-round [])))
