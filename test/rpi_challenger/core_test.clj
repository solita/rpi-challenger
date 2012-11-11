(ns rpi-challenger.core-test
  (:require [clojure.contrib.string :as string])
  (:use clojure.test
        rpi-challenger.core))

(deftest routes-test

  (testing "Shows index page"
    (let [req {:uri "/"}
          resp (routes req)]
      (is (= 200 (:status resp)))
      (is (string/substring? "index" (:body resp)))))

  (testing "Shows Error 404 when page not found"
    (let [req {:uri "/no-such-page"}
          resp (routes req)]
      (is (= 404 (:status resp)))
      (is (= "Page Not Found" (:body resp)))))

  )
