(ns rpi-challenger.challenges-test
  (:use clojure.test
        rpi-challenger.challenges))

(deftest hello-world-test

  (testing "says hello"
    (let [x (hello-world "everybody")]
      (is (= "Say hello to everybody" (:challenge x)))
      (is (= "Hello everybody" (:answer x)))))

  (testing "is randomized"
    (let [xs (repeatedly 100 #(hello-world))]
      (is (< 1 (count (distinct xs)))))))
