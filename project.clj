(defproject funcool/httpurr "0.6.2"
  :description "A ring-inspired, promise-returning, simple Clojure(Script) HTTP client."
  :url "http://funcool.github.io/httpurr"
  :license {:name "Public Domain" :url "http://unlicense.org"}
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.225" :scope "provided"]
                 [aleph "0.4.1" :scope "provided"]
                 [org.clojure/test.check "0.9.0" :scope "test"]
                 [funcool/promesa "1.5.0"]]

  :profiles
  {:dev
   {:plugins [[lein-ancient "0.6.10"]]}})
