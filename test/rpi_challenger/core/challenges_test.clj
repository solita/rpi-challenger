(ns rpi-challenger.core.challenges-test
  (:use clojure.test)
  (:require [rpi-challenger.core.challenges :as c]
            [rpi-challenger.core.dummy :as dummy]))

(defn not-challenge [])

(deftest challenges-test

  (testing "Identifies challenge functions"
    (is (c/challenge? (var c/ping)))
    (is (c/challenge? (var dummy/challenge-42)))
    (is (not (c/challenge? (var not-challenge)))))

  (testing "Reads price from the challenge function"
    (is (= 0 (c/price (var c/ping))))
    (is (= 42 (c/price (var dummy/challenge-42)))))

  (testing "Finds public challenges from all namespaces"
    (def challenges (c/find-challenge-functions))
    (is (some #(= (var c/ping) %) challenges))
    (is (some #(= (var dummy/challenge-42) %) challenges)))

  (testing "Challenge instance contains :price, :question and :answer"
    (is (= {:price 42
            :question ["Answer to life, universe and everything?"]
            :answer "forty-two"}
          (c/generate (var dummy/challenge-42)))))

  (testing "Each question element is put into its own line"
    (let [challenge {:question ["+" "1" "2"]}]
      (is (= "+\n1\n2" (c/format-question challenge)))))

  (testing "Integer question elements are acceptable"
    (let [challenge {:question ["+" 1 2]}]
      (is (= "+\n1\n2" (c/format-question challenge))))))
