(defproject tibco-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [coders-at-work/ladybird "0.5.3-SNAPSHOT"]
                 [org.clojure/tools.cli "0.3.5"]
                 [redbull-transporter "0.1.0-SNAPSHOT"]
                 [cheshire "5.8.0"]
                 [clj-time "0.14.2"]
                 ]
  :main ^:skip-aot tibco-test.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
