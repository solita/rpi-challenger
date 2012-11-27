(ns rpi-challenger.util.io
  (:use [clojure.pprint :only [pprint]])
  (:require [clojure.java.io :as io]))

(defn object-to-file [filename object]
  (with-open [w (io/writer filename)]
    (pprint object w)))

(defn file-to-object [filename]
  (with-open [r (java.io.PushbackReader. (io/reader filename))]
    (binding [*read-eval* false]
      (read r))))
