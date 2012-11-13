(ns rpi-challenger.views-test
  (:use clojure.test
        rpi-challenger.views
        net.cgrand.enlive-html
        [clojure.pprint :only [pprint]])
  (:require [clojure.contrib.string :as string]))

(defn- select-only
  [node-or-nodes selector]
  (let [results (select node-or-nodes selector)]
    (assert
      (= 1 (count results))
      (str "Expected exactly one result but got " (seq results)))
    (first results)))

(defn- attr-value
  [attr node]
  (attr (:attrs node)))


(deftest overview-test

  (testing "Shows service information"
    (let [service {:name "The One" :url "http://the-url" :score 42}
          actual-nodes (services-row service)]
      (is (= "The One" (select-only actual-nodes [:a :> text-node])))
      (is (= "http://the-url" (attr-value :href (select-only actual-nodes [:a ]))))
      (is (= "42" (select-only actual-nodes [[:td (nth-of-type 2)] :> text-node]))))))
