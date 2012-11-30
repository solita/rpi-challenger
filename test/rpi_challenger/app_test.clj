(ns rpi-challenger.app-test
  (:use clojure.test
        rpi-challenger.util.testing)
  (:require [rpi-challenger.app :as app]
            [rpi-challenger.util.threads :as threads])
  (:import [java.io File]))

(deftest app-test
  (binding [app/logger org.slf4j.helpers.NOPLogger/NOP_LOGGER]

    (testing "Participants may register to the tournament"
      (let [app (app/make-app)

            id (app/register-participant app "The Name" "http://the-url")]

        (is (= 1 (count (app/get-participants app))))
        (is (= "The Name" (:name (app/get-participant-by-id app id))))))

    (testing "Starts a new round at regular intervals"
      (let [app (app/make-app)
            scheduled-function (ref nil)
            scheduled-delay (ref nil)]
        (binding [threads/schedule-with-fixed-delay
                  (fn [scheduler f delay]
                    (dosync
                      (ref-set scheduled-function f)
                      (ref-set scheduled-delay delay)))]

          (app/start app)

          (is (calls? app/start-new-round (@scheduled-function)))
          (is (= app/round-duration-in-seconds @scheduled-delay)))))

    (testing "Starts polling participants when they register"
      (let [app (app/make-app)
            executed-function (ref nil)]
        (binding [threads/execute
                  (fn [executor f]
                    (dosync
                      (ref-set executed-function f)))]

          (app/register-participant app "The Name" "http://the-url")

          (is (calls? app/poll-participant-loop (@executed-function))))))

    (testing "Application state can be persisted between restarts"
      (let [app (app/make-app)
            file (File/createTempFile "app-persistence" nil)]
        (try
          (do
            (app/register-participant app "The Name" "http://the-url")

            (app/save-state app file)

            (is (= (app/get-participants app)
                  (app/get-participants (app/load-state file)))))
          (finally
            (.delete file)))))))

; TODO: poll-participant
