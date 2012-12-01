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
      (let [round (round/finish [])]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (is (nil? (:significant-hit round)))
        (is (nil? (:worst-failure round)))))

    (testing "Round with only failures"
      (let [round (round/finish [(failure 8) (failure 5) (failure 10)])]
        (testing "is worth nothing"
          (is (= 0 (:points round))))
        (testing "The worst failure is the failure with lowest points"
          (is (nil? (:significant-hit round)))
          (is (= (failure 5) (:worst-failure round))))))

    (testing "Round with only passes"
      (let [round (round/finish [(pass 3) (pass 7) (pass 5)])]
        (testing "is worth the maximum challenge points"
          (is (= 7 (:points round))))
        (testing "The significant hit is the hit with highest points"
          (is (= (pass 7) (:significant-hit round)))
          (is (nil? (:worst-failure round))))))

    (testing "Round with passes and failures"
      (let [round (round/finish [(pass 1) (pass 3) (pass 5) (failure 5)])]
        (testing "is worth the maximum challenge points below the worst failure"
          (is (= 3 (:points round))))
        (testing "The significant hit is the most valuable hit below the worst failure"
          (is (= (pass 3) (:significant-hit round)))
          (is (= (failure 5) (:worst-failure round))))))))
