(ns rpi-challenger.rating-test
  (:use clojure.test
        rpi-challenger.rating))

(deftest correct?-test
  (let [challenge {:question "the question"
                   :answer "correct answer"}]

    (testing "correct response"
      (let [response {:body "correct answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (correct? response challenge))))

    (testing "wrong response"
      (let [response {:body "wrong answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (not (correct? response challenge)))))

    (testing "wrong status code"
      (let [response {:body "correct answer"
                      :status {:code 202, :msg "Accepted"}
                      :error nil}]
        (is (not (correct? response challenge)))))

    (testing "connection error"
      (let [response {:body nil
                      :status nil
                      :error "java.net.ConnectException: http://foo/"}]
        (is (not (correct? response challenge)))))))

(deftest score-current-round-test
  (binding [correct? (fn [response challenge] (= response "correct"))]
    (let [participant {:score 100
                       :current-round [{:response "correct"}
                                       {:response "correct"}
                                       {:response "fail"}
                                       {:response "correct"}]}

          participant (score-current-round participant)]

      (testing "adds the score for the current round to the total score"
        (is (= 103 (:score participant))))
      (testing "resets the current round"
        (is (empty? (:current-round participant)))))))
