(ns rpi-challenger.core.strike)

(defn make-strike
  [response challenge]
  {:timestamp (System/currentTimeMillis),
   :response response,
   :challenge challenge})
