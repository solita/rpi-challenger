(ns rpi-challenger.core.rating
  (:require [clojure.string :as string]))

(defn ^:dynamic correct?
  [response challenge]
  (and
    (nil? (:error response))
    (= 200 (:code (:status response)))
    (= (string/trim-newline (:body response)) (:answer challenge))))
