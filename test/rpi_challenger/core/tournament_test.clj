(ns rpi-challenger.core.tournament-test
  (:use clojure.test)
  (:require [rpi-challenger.core.tournament :as t]
            [rpi-challenger.rating :as rating]))

(deftest tournament-test
  (let [any-participant {:name "Somebody", :url "http://somewhere"}
        any-challenge {:question "ping", :answer "pong"}
        any-response {:body "wut?"}
        correct-response "correct"
        failed-response "failed"

        tournament (t/make-tournament)]

    (testing "Initially tournament has no participants"
      (is (empty? (t/participants tournament))))

    (testing "Participants can register to the tournament"
      (let [tournament (t/register-participant tournament any-participant)

            participants (t/participants tournament)
            participant (first participants)]
        (is (= 1 (count participants)))
        (is (= "Somebody" (:name participant)))
        (is (= "http://somewhere" (:url participant)))

        (testing "Strikes can be recorded to a participant"
          (let [tournament (t/record-strike tournament participant any-response any-challenge)
                tournament (t/record-strike tournament participant any-response any-challenge)

                strike (first (t/strikes tournament participant))]
            (is (< 1 (:timestamp strike)))
            (is (= any-challenge (:challenge strike)))
            (is (= any-response (:response strike)))))

        (testing "When the current round is finished,"
          (binding [rating/score-strikes (fn [strikes] (assert (= 3 (count strikes))) 42)]
            (let [tournament (t/record-strike tournament participant correct-response any-challenge)
                  tournament (t/record-strike tournament participant correct-response any-challenge)
                  tournament (t/record-strike tournament participant failed-response any-challenge)

                  tournament (t/finish-current-round tournament)]
              (testing "adds the score for the current round to the total score"
                (is (= 42 (:score (first (t/participants tournament))))))
              (testing "starts a new round"
                (is (empty? (t/strikes tournament participant)))))))))))
