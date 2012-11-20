(ns rpi-challenger.io
  (:use [clojure.pprint :only [pprint]]))

(defn object-to-file [filename object]
  (with-open [w (clojure.java.io/writer filename)]
    (pprint object w)))

(defn file-to-object [filename]
  (with-open [r (java.io.PushbackReader. (clojure.java.io/reader filename))]
    (binding [*read-eval* false]
      (read r))))
