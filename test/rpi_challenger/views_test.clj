(ns rpi-challenger.views-test
  (:use clojure.test
        rpi-challenger.views
        net.cgrand.enlive-html
        [clojure.pprint :only [pprint]])
  (:require [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.dummy :as dummy]))

(defn- select1
  [node-or-nodes selector]
  (let [results (select node-or-nodes selector)]
    (assert
      (= 1 (count results))
      (str "Expected exactly one result but got " (seq results)))
    (first results)))

(defn- get-content
  [node-or-nodes selector]
  (apply str (select node-or-nodes [selector :> text-node])))

(defn- get-attr
  [attr node]
  (attr (:attrs node)))


(deftest participants-row-test
  (let [participant {:name "The One" :url "http://the-url" :score 42}
        html (participants-row participant)]
    (is (= "The One" (get-content html *participant-link)))
    (is (= "http://the-url" (get-attr :href (select1 html *participant-link))))
    (is (= "42" (get-content html *participant-score)))))

(deftest strikes-row-test
  (testing "Formatting status codes"
    (is (= "200 OK" (format-status-or-error dummy/hit-strike)))
    (is (= "404 Not Found" (format-status-or-error dummy/fail-strike)))
    (is (= "Connection was closed" (format-status-or-error dummy/error-strike)))))

;(println (strikes-row dummy/hit-strike))
