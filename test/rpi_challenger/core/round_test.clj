(ns rpi-challenger.core.round-test
  (:use clojure.test)
  (:require [rpi-challenger.core.round :as round]
            [rpi-challenger.core.strike :as strike]))

; dummy strikes
(defn- pass [points] {:hit true, :points points})
(defn- failure [points] {:hit false, :points points})

(deftest round-test
  (binding [strike/hit? :hit, strike/points :points ]

    (testing "Round with no strikes"
      (let [round
            (->
              (round/start)
              (round/finish))]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (is (nil? (:significant-hit round)))
        (is (nil? (:worst-failure round)))))

    (testing "Round with only failures"
      (let [round
            (->
              (round/start)
              (round/record-strike (failure 8))
              (round/record-strike (failure 5))
              (round/record-strike (failure 10))
              (round/finish))]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (testing "The worst failure is the failure with lowest points"
          (is (nil? (:significant-hit round)))
          (is (= (failure 5) (:worst-failure round))))))

    (testing "Round with only passes"
      (let [round
            (->
              (round/start)
              (round/record-strike (pass 3))
              (round/record-strike (pass 7))
              (round/record-strike (pass 5))
              (round/finish))]
        (testing "is worth the maximum challenge points"
          (is (= 7 (:points round))))
        (testing "The significant hit is the hit with highest points"
          (is (= (pass 7) (:significant-hit round)))
          (is (nil? (:worst-failure round))))))

    (testing "Round with passes and failures"
      (let [round
            (->
              (round/start)
              (round/record-strike (pass 1))
              (round/record-strike (pass 3))
              (round/record-strike (pass 5))
              (round/record-strike (failure 5))
              (round/finish))]
        (testing "is worth the maximum challenge points below the worst failure"
          (is (= 3 (:points round))))
        (testing "The significant hit is the most valuable hit below the worst failure"
          (is (= (pass 3) (:significant-hit round)))
          (is (= (failure 5) (:worst-failure round))))))))
