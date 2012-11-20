(ns rpi-challenger.io-test
  (:use clojure.test)
  (:use rpi-challenger.io))

(deftest object-serialization-test
  (let [file (java.io.File/createTempFile "io-test" nil)
        original {:foo "asdf", :bar [1 2 3]}]
    (object-to-file file original)
    (is (= original (file-to-object file)))))
