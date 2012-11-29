(ns rpi-challenger.util.testing)

(defmacro calls? [expected-fn & body]
  `(let [called# (ref false)]
     (binding [~expected-fn (fn [& args#] (dosync (ref-set called# true)))]
       ~@body
       @called#)))
