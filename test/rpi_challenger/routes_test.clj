(ns rpi-challenger.routes-test
  (:use clojure.test
        rpi-challenger.routes)
  (:require [rpi-challenger.app :as app]))

(defn- GET [uri webapp]
  (webapp {:request-method :get
           :uri uri
           :headers {}}))

(defn- POST [uri params webapp]
  (webapp {:request-method :post
           :uri uri
           :headers {}
           :params params}))

(defn- redirect-location [response]
  (get-in response [:headers "Location"]))


(deftest routes-test
  (binding [app/register-participant (fn [& _])]
    (let [webapp (make-routes (app/make-app))]

      (testing "Shows index page"
        (let [response (GET "/" webapp)]
          (is (= 200 (:status response)))
          (is (re-find #"Tournament Overview" (:body response)))))

      (testing "Serves static resources"
        (let [response (GET "/layout.html" webapp)]
          (is (= 200 (:status response)))
          (is (= "layout.html" (.getName (:body response))))))

      (testing "Shows Error 404 when page not found"
        (let [response (GET "/no-such-page" webapp)]
          (is (= 404 (:status response)))
          (is (= "404 Page Not Found" (:body response)))))

      (testing "Registration form"

        (testing "Fails if name missing"
          (let [response (POST "/register" {"name" "" "url" "the-url"} webapp)]
            (is (= 302 (:status response)))
            (is (= "/?message=Registration failed" (redirect-location response)))))

        (testing "Fails if URL missing"
          (let [response (POST "/register" {"name" "the-name" "url" ""} webapp)]
            (is (= 302 (:status response)))
            (is (= "/?message=Registration failed" (redirect-location response)))))

        (testing "Succeeds if all parameters are non-empty"
          (let [response (POST "/register" {"name" "the-name" "url" "the-url"} webapp)]
            (is (= 302 (:status response)))
            (is (= "/" (redirect-location response)))))))))
