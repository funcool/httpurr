(defproject funcool/httpurr "0.6.3"
  :description "A ring-inspired, promise-returning, simple Clojure(Script) HTTP client."
  :url "http://funcool.github.io/httpurr"
  :license {:name "Public Domain" :url "http://unlicense.org"}
  :source-paths ["src"]
  :dependencies
    [[aleph "0.4.1" :scope "provided"]
     [funcool/promesa "1.5.0"]
     [lein-doo "0.1.7"]
     [org.clojure/clojure "1.8.0" :scope "provided"]
     [org.clojure/clojurescript "1.9.225" :scope "provided"]
     [org.clojure/test.check "0.9.0" :scope "test"]]


  ;;
  :cljsbuild
    {:builds
      [; The path to the top-level ClojureScript source directory:
       {:id "node-test"
        :source-paths ["src/httpurr" "test/httpurr"]
        ; The standard ClojureScript compiler options:
        ; (See the ClojureScript compiler documentation for details.)
        :compiler
         {:output-to "target/testable.js"
          :output-dir "target"
          :main httpurr.test.runner
          :target :nodejs
          :pretty-print true}}]}

  :profiles
  {:dev
   {:plugins
     [[lein-ancient "0.6.10"]
      [lein-doo "0.1.7"]
      [lein-cljsbuild "1.1.5"]]}})
