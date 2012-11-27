(ns rpi-challenger.util.io-test
  (:use clojure.test
        rpi-challenger.util.io)
  (:import [java.io File]))

(deftest object-serialization-test
  (let [file (File/createTempFile "io-test" nil)
        original {:foo "asdf", :bar [1 2 3]}]
    (object-to-file file original)
    (is (= original (file-to-object file)))))
