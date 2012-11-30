(ns rpi-challenger.http-test
  (:use clojure.test)
  (:require [rpi-challenger.http :as http]))

(deftest post-request-test

  (testing "Returns an error message on internal network library error"
    (let [response (http/post-request "<malformed url>" "")]
      (is (nil? (:body response)))
      (is (nil? (:status response)))
      (is (re-matches #".*IllegalArgumentException.*" (:error response))))))
