(ns rpi-challenger.challenges
  (:require [clojure.tools.namespace.find :as ns-find])
  (:import [java.io File]))

; TODO: parameterize on command line or create an admin screen
(def ^:dynamic *challenges-dir* "../rpi-challenges/src/")

(defn challenge?
  [f]
  (contains? (meta f) :challenge ))

(defn points
  [challenge-f]
  (:challenge (meta challenge-f)))

(defn generate
  [challenge-f]
  (assoc (challenge-f) :points (points challenge-f)))

(defn load-challenge-functions
  []
  (doseq [source-file (ns-find/find-clojure-sources-in-dir (File. *challenges-dir*))]
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
  {:question "ping"
   :answer "pong"})
