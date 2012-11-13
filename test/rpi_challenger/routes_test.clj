(ns rpi-challenger.routes-test
  (:use clojure.test
        rpi-challenger.routes)
  (:require [clojure.contrib.string :as string]))

(defn- GET [uri app]
  (app {:request-method :get
        :uri uri
        :headers {}}))

(defn- POST [uri params app]
  (app {:request-method :post
        :uri uri
        :headers {}
        :params params}))

(defn- redirect-location [response]
  (get-in response [:headers "Location"]))


(deftest routes-test

  (testing "Shows index page"
    (let [response (GET "/" app)]
      (is (= 200 (:status response)))
      (is (string/substring? "Raspberry Pi Challenger" (:body response)))))

  (testing "Serves static resources"
    (let [response (GET "/layout.html" app)]
      (is (= 200 (:status response)))
      (is (= "layout.html" (.getName (:body response))))))

  (testing "Shows Error 404 when page not found"
    (let [response (GET "/no-such-page" app)]
      (is (= 404 (:status response)))
      (is (= "Page Not Found" (:body response)))))

  (testing "Registration form"

    (testing "Fails if name missing"
      (let [response (POST "/register" {"name" "" "url" "the-url"} app)]
        (is (= 302 (:status response)))
        (is (= "/?message=Registration failed" (redirect-location response)))))

    (testing "Fails if URL missing"
      (let [response (POST "/register" {"name" "the-name" "url" ""} app)]
        (is (= 302 (:status response)))
        (is (= "/?message=Registration failed" (redirect-location response)))))

    (testing "Succeeds if all parameters are non-empty"
      (let [response (POST "/register" {"name" "the-name" "url" "the-url"} app)]
        (is (= 302 (:status response)))
        (is (= "/?message=OK" (redirect-location response)))))))
