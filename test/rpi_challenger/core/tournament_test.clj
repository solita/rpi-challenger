(ns rpi-challenger.core.tournament-test
  (:use clojure.test)
  (:require [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.rating :as rating]))

(deftest tournament-test
  (let [any-participant (p/make-participant "Somebody" "http://somewhere")
        any-challenge {:question "ping", :answer "pong"}
        correct-response "correct"
        failed-response "failed"
        hit (s/make-strike correct-response any-challenge)
        miss (s/make-strike failed-response any-challenge)

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

        (testing "Can query participants by id"
          (is (= participant (t/participant-by-id tournament (:id participant)))))

        (testing "Strikes can be recorded to a participant"
          (let [tournament (t/record-strike tournament participant hit)
                tournament (t/record-strike tournament participant miss)]
            (is (= [hit miss] (p/current-round (t/participant-by-id tournament (:id participant)))))))

        (testing "When the current round is finished,"
          (binding [rating/score-strikes (fn [strikes] (assert (= 3 (count strikes))) 42)]
            (let [tournament (t/record-strike tournament participant hit)
                  tournament (t/record-strike tournament participant hit)
                  tournament (t/record-strike tournament participant miss)

                  tournament (t/finish-current-round tournament)]
              (testing "adds the score for the current round to the total score"
                (is (= 42 (:score (first (t/participants tournament))))))
              (testing "starts a new round"
                (is (empty? (p/current-round (t/participant-by-id tournament (:id participant)))))))))))))
