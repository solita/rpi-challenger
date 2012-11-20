(defproject rpi-challenger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/solita"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [http.async.client "0.5.0-SNAPSHOT"]
                 [ch.qos.logback/logback-classic "1.0.7"]
                 [ring/ring-core "1.1.6"]
                 [ring/ring-devel "1.1.6"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [compojure "1.1.3"]
                 [enlive "1.0.0"]]

  :main rpi-challenger.main)
