(ns rpi-challenger.app-test
  (:use clojure.test
        rpi-challenger.util.testing)
  (:require [rpi-challenger.app :as app]
            [rpi-challenger.core.strike :as strike]
            [rpi-challenger.http :as http]
            [rpi-challenger.util.threads :as threads])
  (:import [java.io File]))

(deftest app-test
  (binding [app/logger org.slf4j.helpers.NOPLogger/NOP_LOGGER
            threads/submit (fn [& _])
            threads/schedule-repeatedly (fn [& _])]

    (testing "Participants may register to the tournament"
      (let [app (app/make-app)]
        (app/register-participant app "The Name" "http://the-url")

        (is (= 1 (count (app/get-participants app))))
        (is (= "The Name" (:name (app/get-participant-by-id app 1))))))

    (testing "At regular intervals"
      (let [app (app/make-app)
            scheduled-functions (ref [])
            scheduled-delays (ref [])]
        (binding [threads/schedule-repeatedly
                  (fn [scheduler f delay]
                    (dosync
                      (alter scheduled-functions conj f)
                      (alter scheduled-delays conj delay)))]

          (app/start app)

          (testing "starts a new round"
            (is (calls? app/start-new-round ((first @scheduled-functions))))
            (is (= app/round-duration-in-seconds (first @scheduled-delays))))

          (testing "saves application state"
            (is (calls? app/save-state ((second @scheduled-functions))))
            (is (= app/save-interval-in-seconds (second @scheduled-delays)))))))

    (testing "Starts polling participants when they register"
      (let [app (app/make-app)
            executed-function (ref nil)]
        (binding [threads/submit
                  (fn [executor f]
                    (dosync
                      (ref-set executed-function f)))]

          (app/register-participant app "The Name" "http://the-url")

          (is (calls? app/poll-participant-loop (@executed-function))))))

    (testing "Sends each challenge to the participant until encounters the first failure"
      (with-local-vars [recorded-responses []]
        (let [app (app/make-app)
              participant {:url "foo"}]
          (binding [http/post-request (fn [url body] (not (= "fail" body)))
                    app/record-strike (fn [app participant strike] (append recorded-responses (:response strike)))
                    strike/hit? (fn [strike] (:response strike))]

            (app/poll-participant app participant [{:question ["dummy" "pass1"]}
                                                   {:question ["dummy" "pass2"]}
                                                   {:question ["dummy" "fail"]}
                                                   {:question ["dummy" "pass3"]}])

            (is (= [true true false] @recorded-responses))))))

    (testing "Application state can be persisted between restarts"
      (let [original (app/make-app)
            file (File/createTempFile "app-persistence" nil)]
        (try
          (do
            (app/register-participant original "The Name" "http://the-url")

            (app/save-state original file)
            (let [restarted (app/load-state file)]

              (testing "Participants are persisted"
                (is (= (app/get-participants original)
                       (app/get-participants restarted))))

              (testing "Starts polling participants on starting a new round"
                (is (calls? app/start-polling (app/start-new-round restarted))))))

          (finally
            (.delete file)))))))
