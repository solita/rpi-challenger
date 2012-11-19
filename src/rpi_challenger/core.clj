(ns rpi-challenger.core)

; TODO: remove this dummy data
(defonce services (ref {"http://foo" {:name "Foo" :url "http://foo" :score 0}}))

(defn add-service
  [services name url]
  (assoc services url {:name name :url url :score 0}))

(defn register
  [name url]
  (dosync
    (alter services add-service name url)))

(defn get-services
  []
  (vals (deref services)))
