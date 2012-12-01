(ns rpi-challenger.core.tournament-test
  (:use clojure.test)
  (:require [rpi-challenger.core.tournament :as t]
            [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.round :as round]))

(deftest tournament-test
  (let [any-challenge {:question ["ping"], :answer "pong"}
        correct-response "correct"
        failed-response "failed"
        hit (s/make-strike correct-response any-challenge)
        miss (s/make-strike failed-response any-challenge)

        tournament (t/make-tournament)]

    (testing "Initially tournament has no participants"
      (is (empty? (t/participants tournament))))

    (testing "Participants can register to the tournament"
      (let [tournament (t/register-participant tournament (t/make-participant tournament "Somebody" "http://somewhere"))

            participants (t/participants tournament)
            participant (first participants)]
        (is (= 1 (count participants)))
        (is (= "Somebody" (:name participant)))
        (is (= "http://somewhere" (:url participant)))

        (testing "Each participant gets a unique ID"
          (let [tournament (t/register-participant tournament (t/make-participant tournament "Another" "http://anywhere"))
                [participant-1 participant-2] (t/participants tournament)]
            (is (= 1 (:id participant-1)))
            (is (= 2 (:id participant-2)))))

        (testing "Can query participants by id"
          (is (= participant (t/participant-by-id tournament (:id participant)))))

        (testing "Strikes can be recorded to a participant"
          (let [tournament (t/record-strike tournament participant hit)
                tournament (t/record-strike tournament participant miss)]
            (is (= [hit miss] (p/recent-strikes (t/participant-by-id tournament (:id participant)))))))

        (testing "When the current round is finished,"
          (binding [round/finish (fn [strikes] (assert (= 3 (count strikes))) {:points 42})]
            (let [tournament (t/record-strike tournament participant hit)
                  tournament (t/record-strike tournament participant hit)
                  tournament (t/record-strike tournament participant miss)

                  tournament (t/finish-current-round tournament)]
              (testing "adds the score for the current round to the total score"
                (is (= 42 (:score (first (t/participants tournament)))))))))

        (testing "Participants can be persisted"
          (let [deserialized (t/deserialize (t/serialize tournament))]
            (is (= (t/participants tournament)
                  (t/participants deserialized)))))))))

(deftest challenges-test
  (require 'rpi-challenger.core.dummy) ; making sure that we have at least two challenges loaded)
  (let [tournament (t/make-tournament)
        tournament (t/update-challenge-functions tournament)
        challenges (t/generate-challenges tournament)]
    (is (< 1 (count challenges)))

    (testing "Challenges start with the ping challenge"
      (is (= ["ping"] (:question (first challenges)))))

    (testing "Challenges are in increasing difficulty order"
      (let [points (map :points challenges)]
        (is (= (sort points) points))))))
