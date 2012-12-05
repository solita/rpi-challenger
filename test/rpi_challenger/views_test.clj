(ns rpi-challenger.views-test
  (:use clojure.test
        rpi-challenger.views
        net.cgrand.enlive-html
        [clojure.pprint :only [pprint]])
  (:require [rpi-challenger.core.participant :as p]
            [rpi-challenger.core.strike :as s]
            [rpi-challenger.core.dummy :as dummy]))

(defn- select1
  [node-or-nodes selector]
  (let [results (select node-or-nodes selector)]
    (assert
      (= 1 (count results))
      (str "Expected exactly one result but got " (seq results)))
    (first results)))

(defn- get-content
  [node-or-nodes selector]
  (apply str (select node-or-nodes [selector :> text-node])))

(defn- get-attr
  [attr node]
  (attr (:attrs node)))

(deftest tournament-overview-page-test
  (testing "Shows registration error messages"
    (let [html (apply str (tournament-overview-page [] "Some error message"))]
      (is (re-find #"<div id=\"error-message\">Some error message</div>" html))))

  (testing "Doesn't show the error message element when there is no message"
    (let [html (apply str (tournament-overview-page [] nil))]
      (is (not (re-find #"error-message" html))))))

(deftest participants-row-test
  (let [participant {:id 100, :name "The Name", :url "http://the-url", :score 42}
        html (participants-row participant)]
    (is (= "The Name" (get-content html *participant-link)))
    (is (= "/participant-100" (get-attr :href (select1 html *participant-link))))
    (is (= "42" (get-content html *participant-score)))))

(deftest strikes-row-test
  (testing "Formatting status codes"
    (is (= "200 OK" (format-status-or-error dummy/hit-strike)))
    (is (= "404 Not Found" (format-status-or-error dummy/fail-strike)))
    (is (= "Connection was closed" (format-status-or-error dummy/error-strike)))))

;(println (strikes-row dummy/hit-strike))
