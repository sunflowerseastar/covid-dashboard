(defproject covid-dashboard "0.1.0-SNAPSHOT"
  :jvm-opts ["-Dnashorn.args=--language=es6"]
  :dependencies [[breaking-point "0.1.2"]
                 [clj-commons/secretary "1.2.4"]
                 [cljsjs/d3 "5.12.0-0"
                  :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server cljsjs/create-react-class]]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [day8.re-frame/http-fx "v0.2.0"]
                 [day8.re-frame/tracing "0.5.5"]
                 [garden "1.3.10"]
                 [applied-science/js-interop "0.2.7"]
                 [ns-tracker "0.4.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.764"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [re-com "2.8.0"]
                 [re-frame "0.12.0"]
                 [reagent "0.10.0"]
                 [tupelo "0.9.214"]
                 [thheller/shadow-cljs "2.9.3"]]

  :plugins [[lein-garden "0.3.0"]
            [lein-shadow "0.2.0"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.9.0"

  :source-paths ["src/clj" "src/cljs"]

  :test-paths   ["test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]


  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   covid-dashboard.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :shadow-cljs {:nrepl {:port 8777}
                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :modules {:app {:init-fn covid-dashboard.core/init
                                               :preloads [devtools.preload
                                                          day8.re-frame-10x.preload]}}
                               :dev {:compiler-options {:closure-defines {re-frame.trace.trace-enabled? true day8.re-frame.tracing.trace-enabled? true}
                                                        :externs ["externs/externs.js"]
                                                        :language-in :es6
                                                        :rewrite-polyfills true
                                                        :warnings {:fn-arity false :undeclared-var false}}}
                               :release {:compiler-options {:optimizations :advanced}
                                         :build-options
                                         {:ns-aliases
                                          {day8.re-frame.tracing day8.re-frame.tracing-stubs}}}

                               :devtools {:http-root "resources/public"
                                          :http-port 8280}}

                         :browser-test
                         {:target :browser-test
                          :ns-regexp "-test$"
                          :runner-ns shadow.test.browser
                          :test-dir "target/browser-test"
                          :devtools {:http-root "target/browser-test"
                                     :http-port 8290}}

                         :karma-test
                         {:target :karma
                          :ns-regexp "-test$"
                          :output-to "target/karma-test.js"}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["shadow" "watch" "app"]]
            "prod"         ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]
            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]
            "karma"        ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.0"] [day8.re-frame/re-frame-10x "0.6.5"]]
                   :source-paths ["dev"]}
             :prod {}}

  :prep-tasks [["garden" "once"]])
