(ns rpi-challenger.core-test
  (:use clojure.test
        rpi-challenger.core))

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
