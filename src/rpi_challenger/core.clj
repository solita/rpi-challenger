(ns rpi-challenger.core
  (:use ring.util.response
        net.cgrand.enlive-html
        [net.cgrand.moustache :only [app]]))

(deftemplate layout "resources/layout.html" [body]
  [:#content ] (content body))

(defn html-page [template & args]
  (->
    (response (apply str (apply template args)))
    (content-type "text/html")
    (charset "UTF-8")))

(def routes
  (app
    [""] (fn [req] (html-page layout "Hello world!"))
    [&] (fn [req] (->
                    (not-found "Page Not Found")
                    (content-type "text/html")
                    (charset "UTF-8")))))
