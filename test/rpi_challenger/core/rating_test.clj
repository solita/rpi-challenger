(ns rpi-challenger.core.rating-test
  (:use clojure.test)
  (:require [rpi-challenger.core.rating :as rating]
            [rpi-challenger.core.participant :as p]))

(deftest correct?-test
  (let [challenge {:question "the question"
                   :answer "correct answer"}]

    (testing "correct response"
      (let [response {:body "correct answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (rating/correct? response challenge))))

    (testing "wrong response"
      (let [response {:body "wrong answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (not (rating/correct? response challenge)))))

    (testing "wrong status code"
      (let [response {:body "correct answer"
                      :status {:code 202, :msg "Accepted"}
                      :error nil}]
        (is (not (rating/correct? response challenge)))))

    (testing "connection error"
      (let [response {:body nil
                      :status nil
                      :error "java.net.ConnectException: http://foo/"}]
        (is (not (rating/correct? response challenge)))))

    (testing "ignores trailing newline in the answer"
      (let [response {:body "correct answer\r\n"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (rating/correct? response challenge))))

    (testing "doesn't ignore other whitespace in the answer"
      (let [response {:body "correct answer "
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (not (rating/correct? response challenge)))))))

(deftest score-current-round-test
  (binding [rating/correct? (fn [response challenge] (= response "correct"))]
    (let [participant
          (->
            (p/make-participant 1 "" "")
            (assoc :score 100)
            (p/record-strike {:response "correct"})
            (p/record-strike {:response "correct"})
            (p/record-strike {:response "fail"})
            (p/record-strike {:response "correct"}))]

      (testing "adds the score for the current round to the total score"
        (is (= 103 (:score (rating/score-current-round participant))))))))
