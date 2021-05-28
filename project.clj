(defproject calculator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [metosin/compojure-api "2.0.0-alpha30"]
                 [metosin/schema-tools "0.12.3"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.18.jre7"]
                 [cheshire "5.10.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [environ "1.2.0"]]
  :plugins [[lein-ring "0.12.5"]
            [lein-environ "1.2.0"]]
  :ring {:handler calculator.handler/handler
         :port 3030}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]]
                   :env {:dbname "calculator"
                         :user "sample-web-project"
                         :password "sample-web-project"}
                   :uberjar {:aot :all}}}
  :repl-options {:init-ns calculator.handler})
