(ns rpi-challenger.challenges)

; Hello World reference implementation
(defn- say-hello
  [name]
  (str "Hello " name))

; Hello World challenge generator
(defn hello-world
  ([]
    (hello-world (rand-nth ["World", "Raspberry Pi", "Solita"])))
  ([name]
    {:question (str "Say hello to " name)
     :answer (say-hello name)}))
