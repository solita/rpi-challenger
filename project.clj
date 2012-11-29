(defproject rpi-challenger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/solita"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.4.0"]

                 ; HTTP Server
                 [ring/ring-core "1.1.6"]
                 [ring/ring-devel "1.1.6"]
                 [ring/ring-jetty-adapter "1.1.6"]

                 ; HTTP Routing
                 [compojure "1.1.3"]

                 ; HTML Templating
                 [enlive "1.0.0"]

                 ; HTTP Client
                 [http.async.client "0.5.0-SNAPSHOT"]

                 ; Logging
                 [ch.qos.logback/logback-classic "1.0.7"]

                 ; Date & Time
                 [clj-time "0.4.4"]

                 ; Command Line Interface
                 [org.clojure/tools.cli "0.2.2"]

                 ; Utils
                 [org.clojure/algo.generic "0.1.0"]
                 [org.clojure/tools.namespace "0.2.1"]
                 [com.google.guava/guava "13.0.1"]]

  :main rpi-challenger.main)
