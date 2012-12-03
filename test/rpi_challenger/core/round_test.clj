(ns rpi-challenger.core.round-test
  (:use clojure.test)
  (:require [rpi-challenger.core.round :as round]
            [rpi-challenger.core.strike :as strike]))

; dummy strikes
(defn- hit [price] {:hit true, :error false, :price price})
(defn- miss [price] {:hit false, :error false, :price price})
(defn- error [price] {:hit false, :error true, :price price})

(deftest round-test
  (binding [strike/hit? :hit, strike/error? :error, strike/price :price ]

    (testing "Round contains timestamps of when it was started and finished"
      (let [round
            (->
              (round/start)
              (round/finish))]
        (is (number? (:started round)))
        (is (number? (:finished round)))))

    (testing "Round with no strikes"
      (let [round
            (->
              (round/start)
              (round/finish))]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (is (nil? (:significant-hit round)))
        (is (nil? (:worst-failure round)))))

    (testing "Round with only misses"
      (let [round
            (->
              (round/start)
              (round/record-strike (miss 8))
              (round/record-strike (miss 5))
              (round/record-strike (miss 10))
              (round/finish))]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (testing "The worst failure is the failure with lowest price"
          (is (nil? (:significant-hit round)))
          (is (= (miss 5) (:worst-failure round))))))

    (testing "Round with only hits"
      (let [round
            (->
              (round/start)
              (round/record-strike (hit 3))
              (round/record-strike (hit 7))
              (round/record-strike (hit 5))
              (round/finish))]
        (testing "is worth the maximum challenge price"
          (is (= 7 (:points round))))
        (testing "The significant hit is the hit with highest price"
          (is (= (hit 7) (:significant-hit round)))
          (is (nil? (:worst-failure round))))))

    (testing "Round with hits and misses"
      (let [round
            (->
              (round/start)
              (round/record-strike (hit 1))
              (round/record-strike (hit 3))
              (round/record-strike (hit 5))
              (round/record-strike (miss 5))
              (round/finish))]
        (testing "is worth the maximum challenge price below the worst failure"
          (is (= 3 (:points round))))
        (testing "The significant hit is the most valuable hit below the worst failure"
          (is (= (hit 3) (:significant-hit round)))
          (is (= (miss 5) (:worst-failure round))))))

    (testing "Round with hits and errors"
      (let [round
            (->
              (round/start)
              (round/record-strike (hit 1))
              (round/record-strike (hit 3))
              (round/record-strike (hit 5))
              (round/record-strike (error 5))
              (round/finish))]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (testing "The error makes all hits insignificant"
          (is (nil? (:significant-hit round)))
          (is (= (error 5) (:worst-failure round))))))

    (testing "Round with misses and errors"
      (let [round
            (->
              (round/start)
              (round/record-strike (miss 0))
              (round/record-strike (error 0))
              (round/record-strike (miss 0))
              (round/finish))]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (testing "The error is always the worst thing"
          (is (nil? (:significant-hit round)))
          (is (= (error 0) (:worst-failure round))))))))
