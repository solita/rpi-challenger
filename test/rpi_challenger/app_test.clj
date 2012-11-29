(ns rpi-challenger.app-test
  (:use clojure.test)
  (:require [rpi-challenger.app :as app]))

(deftest app-test
  (binding [app/logger org.slf4j.helpers.NOPLogger/NOP_LOGGER]

    (testing "Participants may register to the tournament"
      (let [app (app/make-app)

            id (app/register-participant app "The Name" "http://the-url")]

        (is (= 1 (count (app/get-participants app))))
        (is (= "The Name" (:name (app/get-participant-by-id app id))))))))
