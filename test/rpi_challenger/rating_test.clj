(ns rpi-challenger.rating-test
  (:use clojure.test
        rpi-challenger.rating))

(deftest correct?-test
  (let [challenge {:challenge "the challenge"
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

(deftest score-tick-test
  (binding [correct? (fn [response challenge] (= response "correct"))]
    (let [service {:score 100
                   :new-events [{:response "correct"}
                                {:response "correct"}
                                {:response "fail"}
                                {:response "correct"}]}

          service (score-tick service)]

      (testing "adds the score for the current tick to the total score"
        (is (= 103 (:score service))))
      (testing "removes the processed events"
        (is (empty? (:new-events service)))))))
