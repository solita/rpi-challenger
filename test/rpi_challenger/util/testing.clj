(ns rpi-challenger.util.testing)

(defn append [var item]
  (var-set var (conj @var item)))

(defmacro calls? [expected-fn & body]
  `(let [called# (ref false)]
     (binding [~expected-fn (fn [& args#] (dosync (ref-set called# true)))]
       ~@body
       @called#)))
