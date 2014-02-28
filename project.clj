(defproject sbornik "0.1.0-SNAPSHOT"
  :description "Sbornik Web Application"
 :url "https://github.com/semperos/sbornik"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.8.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/core.memoize "0.5.6"]
                 [http-kit "2.1.17" :exclusions [org.clojure/clojure]]
                 [compojure "1.1.6" :exclusions [org.clojure/clojure
                                                 org.clojure/tools.reader]]
                 [liberator "0.11.0" :exclusions [org.clojure/clojure
                                                  org.clojure/tools.logging
                                                  hiccup]]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-devel "1.2.1"]
                 ;; Needed for Ring 1.2.x
                 [javax.servlet/servlet-api "2.5"]
                 [hiccup "1.0.5" :exclusions [org.clojure/clojure]]]
  :aliases {"web-watch" ["with-profile" "+web" "cljsbuild" "auto" "dev"]
            "web-repl" ["with-profile" "+web" "trampoline" "cljsbuild" "repl-listen"]
            "web-test" ["with-profile" "+web" "cljsbuild" "auto" "test"]}
  :main ^:skip-aot sbornik.server
  :target-path "target/%s"
  :resource-paths ["resources"
                   "target/client/resources"]
  :plugins [[lein-cljsbuild "1.0.2"]]
  :profiles {:uberjar {:aot :all}
             :web {:dependencies [[org.clojure/clojurescript "0.0-2173"]
                                  [om "0.5.0"]
                                  [com.facebook/react "0.9.0"]
                                  [sablono "0.2.6"]
                                  [secretary "1.0.0"]
                                  [cljs-http "0.1.8"]]}
             :test {:dependencies [[ring-mock "0.1.5"]]}}
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src-cljs"]
                        :compiler {:output-to "target/client/resources/public/app/js/app-dev.js"
                                   :output-dir "target/client/resources/public/app/js"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "test"
                        :source-paths ["src-cljs" "test-cljs"]
                        :compiler {:output-to "target/cljs/app-test.js"
                                   :output-dir "target/cljs"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "production"
                        :source-paths ["src-cljs"]
                        :compiler {:output-to "target/client/resources/public/app/js/app-prod.js"
                                   :output-dir "target/client/resources/public/app/production/js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}]})
