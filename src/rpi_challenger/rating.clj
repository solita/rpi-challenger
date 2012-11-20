(ns rpi-challenger.rating)

(defn ^:dynamic correct?
  [response challenge]
  (and
    (nil? (:error response))
    (= 200 (:code (:status response)))
    (= (:body response) (:answer challenge))))

; TODO: score based on challenge level, then reduce using `min`
(defn score-event
  [event]
  (if (correct? (:response event) (:challenge event))
    1
    0))

(defn score-events
  [events]
  (apply + (map score-event events)))

(defn score-tick
  [service]
  (-> service
    (update-in [:score ] #(+ % (score-events (:new-events service))))
    (assoc :new-events [])))
