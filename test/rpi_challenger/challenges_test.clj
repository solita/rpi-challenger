(ns rpi-challenger.challenges-test
  (:use clojure.test
        rpi-challenger.challenges))

(deftest hello-world-test

  (testing "says hello"
    (let [challenge (hello-world "everybody")]
      (is (= "Say hello to everybody" (:question challenge)))
      (is (= "Hello everybody" (:answer challenge)))))

  (testing "is randomized"
    (let [challenges (repeatedly 100 #(hello-world))]
      (is (< 1 (count (distinct challenges)))))))
