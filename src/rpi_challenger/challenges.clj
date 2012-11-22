(ns rpi-challenger.challenges)

(defn ^{:challenge 0} ping
  []
  {:question "ping"
   :answer "pong"})

; TODO: read the points of a challenge from metadata
;(println (meta (var ping)))
