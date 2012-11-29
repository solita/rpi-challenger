(ns rpi-challenger.app-test
  (:use clojure.test
        rpi-challenger.util.testing)
  (:require [rpi-challenger.app :as app]
            [rpi-challenger.util.threads :as threads]))

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
          (is (= app/round-duration-in-seconds @scheduled-delay)))))))

; TODO: start-new-round
; TODO: poll-participant
