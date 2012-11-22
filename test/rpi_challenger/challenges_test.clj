(ns rpi-challenger.challenges-test
  (:use clojure.test)
  (:require [rpi-challenger.challenges :as c]))

(defn not-challenge [])
(defn ^{:challenge 42} challenge-42 [] {:question "Answer to life, universe and everything?"
                                        :answer "forty-two"})

(deftest challenges-test

  (testing "Identifies challenge functions"
    (is (c/challenge? (var c/ping)))
    (is (c/challenge? (var challenge-42)))
    (is (not (c/challenge? (var not-challenge)))))

  (testing "Reads points from the challenge function"
    (is (= 0 (c/points (var c/ping))))
    (is (= 42 (c/points (var challenge-42)))))

  (testing "Finds public challenges from all namespaces"
    (def challenges (c/find-challenge-functions))
    (is (some #(= (var c/ping) %) challenges))
    (is (some #(= (var challenge-42) %) challenges)))

  (testing "Challenge instance contains :points, :question and :answer"
    (is (= {:points 42
            :question "Answer to life, universe and everything?"
            :answer "forty-two"}
          (c/generate (var challenge-42))))))
