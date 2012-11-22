(ns rpi-challenger.core.participant-test
  (:use clojure.test)
  (:require [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.rating :as rating]))

(deftest participant-test
  (binding [p/*recent-failures-limit* 3
            s/hit? #(= % :hit )]

    (testing "Each participant gets a unique ID"
      (let [participant-1 (p/make-participant "" "")
            participant-2 (p/make-participant "" "")]
        (is (not= (:id participant-1) (:id participant-2)))))

    (let [participant (p/make-participant "Somebody" "http://somewhere")]

      (testing "Recent failures list shows only failures"
        (let [participant
              (-> participant
                (p/record-strike :miss1 )
                (p/record-strike :hit )
                (p/record-strike :miss2 )
                (p/record-strike :miss3 ))]
          (is (= [:miss1 :miss2 :miss3 ]
                (p/recent-failures participant)))))

      (testing "Recent failures list forgets the oldest failures when it gets over the limit"
        (let [participant
              (-> participant
                (p/record-strike :miss1 )
                (p/record-strike :miss2 )
                (p/record-strike :miss3 )
                (p/record-strike :miss4 ))]
          (is (= [:miss2 :miss3 :miss4 ]
                (p/recent-failures participant))))))))
