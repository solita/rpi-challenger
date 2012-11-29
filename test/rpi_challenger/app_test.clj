(ns rpi-challenger.app-test
  (:use clojure.test)
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
            scheduled-delay (ref nil)
            start-new-round-called (ref false)]
        (binding [app/start-new-round
                  (fn [app]
                    (dosync (ref-set start-new-round-called true)))

                  threads/schedule-with-fixed-delay
                  (fn [scheduler f delay]
                    (dosync
                      (ref-set scheduled-function f)
                      (ref-set scheduled-delay delay)))]

          (app/start app)

          (is (= app/round-duration-in-seconds @scheduled-delay))
          (is (= false @start-new-round-called)) ; TODO: extract helper method
          (@scheduled-function)
          (is (= true @start-new-round-called)))))))

; TODO: start-new-round
; TODO: poll-participant
