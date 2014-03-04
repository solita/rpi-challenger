(ns rpi-challenger.core.challenges
  (:require [clojure.tools.namespace.find :as ns-find]
            [clojure.string :as string])
  (:import [java.io File]))


; challenge operations

(defn format-question
  [challenge]
  (string/join "\n" (:question challenge)))


; challenge function operations

(defn challenge?
  [f]
  (contains? (meta f) :challenge))

(defn price
  [challenge-f]
  (:challenge (meta challenge-f)))

(defn generate
  [challenge-f]
  (assoc (challenge-f) :price (price challenge-f)))


; challenge registry

(defn load-challenge-functions
  [^File dir]
  (doseq [source-file (ns-find/find-clojure-sources-in-dir dir)]
    (load-file (.getPath source-file))))

(defn find-challenge-functions
  []
  (let [all-publics (apply concat (map #(vals (ns-publics %)) (all-ns)))
        all-challenges (filter challenge? all-publics)]
    all-challenges))

(defn generate-challenges
  []
  (map generate (find-challenge-functions)))


; Built-in challenges

(defn ^{:challenge 0} ping
  []
  {:question ["ping"]
   :answer   "pong"})
