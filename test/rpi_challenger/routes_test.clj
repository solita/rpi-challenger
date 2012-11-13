(ns rpi-challenger.routes-test
  (:require [clojure.contrib.string :as string])
  (:use clojure.test
        rpi-challenger.routes
        ring.middleware.lint))

(defn- request [uri app]
  (app {:request-method :get
        :uri uri
        :headers {}}))

(deftest routes-test

  (testing "Shows index page"
    (let [response (request "/" app)]
      (is (= 200 (:status response)))
      (is (string/substring? "Raspberry Pi Challenger" (:body response)))))

  (testing "Serves static resources"
    (let [response (request "/layout.html" app)]
      (is (= 200 (:status response)))
      (is (= "layout.html" (.getName (:body response))))))

  (testing "Shows Error 404 when page not found"
    (let [response (request "/no-such-page" app)]
      (is (= 404 (:status response)))
      (is (= "Page Not Found" (:body response)))))

  )
