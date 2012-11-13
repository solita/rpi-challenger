(ns rpi-challenger.core
  (:require [clojure.string :as string]))

(def services (ref {"http://foo" {:name "Foo" :url "http://foo"}}))

(defn add-service
  [services name url]
  (println "Registering" name "with url" url)
  (assoc services url {:name name :url url}))

(defn register
  [name url]
  (dosync
    (alter services add-service name url)))

(defn get-services
  []
  (vals (deref services)))
