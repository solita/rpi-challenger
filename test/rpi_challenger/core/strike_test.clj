(ns rpi-challenger.core.strike-test
  (:use clojure.test
        rpi-challenger.core.strike)
  (:require [rpi-challenger.core.participant :as p]))

(deftest correct-response?-test
  (let [challenge {:question ["the question"]
                   :answer "correct answer"}]

    (testing "correct response"
      (let [response {:body "correct answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (correct-response? response challenge))))

    (testing "wrong response"
      (let [response {:body "wrong answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (not (correct-response? response challenge)))))

    (testing "wrong status code"
      (let [response {:body "correct answer"
                      :status {:code 202, :msg "Accepted"}
                      :error nil}]
        (is (not (correct-response? response challenge)))))

    (testing "connection error"
      (let [response {:body nil
                      :status nil
                      :error "java.net.ConnectException: http://foo/"}]
        (is (not (correct-response? response challenge)))))

    (testing "ignores trailing newline in the answer"
      (let [response {:body "correct answer\r\n"
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (correct-response? response challenge))))

    (testing "doesn't ignore other whitespace in the answer"
      (let [response {:body "correct answer "
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (not (correct-response? response challenge)))))

    (testing "handles null response"
      (let [response {:body nil
                      :status {:code 200, :msg "OK"}
                      :error nil}]
        (is (not (correct-response? response challenge)))))))


(deftest strike-test
  (let [challenge {:question ["the question"]
                   :answer "correct answer"}]

    (testing "correct response is a hit"
      (let [response {:body "correct answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}
            strike (make-strike response challenge)]
        (is (hit? strike))
        (is (not (miss? strike)))
        (is (not (error? strike)))))

    (testing "wrong response is a miss"
      (let [response {:body "wrong answer"
                      :status {:code 200, :msg "OK"}
                      :error nil}
            strike (make-strike response challenge)]
        (is (not (hit? strike)))
        (is (miss? strike))
        (is (not (error? strike)))))

    (testing "error is both a miss and an error"
      (let [response {:body nil
                      :status nil
                      :error "java.net.ConnectException: http://foo/"}
            strike (make-strike response challenge)]
        (is (not (hit? strike)))
        (is (miss? strike))
        (is (error? strike))))))
