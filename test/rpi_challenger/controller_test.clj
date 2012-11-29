(ns rpi-challenger.controller-test
  (:use clojure.test
        rpi-challenger.controller)
  (:require [rpi-challenger.http :as http]))

(defn append [var item]
  (var-set var (conj @var item)))

(deftest poll-participant-test
  (let [participant {:url "participant-url"}
        challenges [{:question "question 1"}
                    {:question "question 2"}]]

    (testing "Polls the participant with each challenge"
      (with-local-vars [requests []]
        (binding [http/post-request (fn [url question] (append requests [url question]))
                  record-response (fn [& _])]

          (poll-participant participant challenges)

          (is (= [["participant-url" "question 1"]
                  ["participant-url" "question 2"]]
                @requests)))))

    (testing "Records responses from the participant"
      (with-local-vars [responses []]
        (binding [http/post-request (fn [url question] (str "response to " question))
                  record-response (fn [participant response challenge] (append responses response))]

          (poll-participant participant challenges)

          (is (= ["response to question 1"
                  "response to question 2"]
                @responses)))))))
