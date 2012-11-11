(ns rpi-challenger.core-test
  (:require [clojure.contrib.string :as string])
  (:use clojure.test
        rpi-challenger.core))

(defn- request [uri app]
  (app {:request-method :get
        :uri uri}))

(deftest routes-test

  (testing "Shows index page"
    (let [response (request "/" app)]
      (is (= 200 (:status response)))
      (is (string/substring? "Raspberry Pi Challenger" (:body response)))))

  (testing "Shows Error 404 when page not found"
    (let [response (request "/no-such-page" app)]
      (is (= 404 (:status response)))
      (is (= "Page Not Found" (:body response)))))

  )
