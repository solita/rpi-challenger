(ns rpi-challenger.http-test
  (:use clojure.test)
  (:require [rpi-challenger.http :as http]))

(deftest post-request-test
  (binding [http/logger org.slf4j.helpers.NOPLogger/NOP_LOGGER]

    (testing "Returns an error message on internal network library error"
      (is (= {:body nil
              :status nil
              :error "java.lang.IllegalArgumentException"}
            (http/post-request "<malformed url>" ""))))))
