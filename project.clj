(defproject funcool/httpurr "2.0.0"
  :description "A ring-inspired, promise-returning, simple Clojure(Script) HTTP client."
  :url "http://funcool.github.io/httpurr"
  :license {:name "Public Domain" :url "http://unlicense.org"}
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.597" :scope "provided"]
                 [aleph "0.4.6" :scope "provided"]
                 [org.martinklepsch/clj-http-lite "0.4.3" :scope "provided"]
                 [org.clojure/test.check "0.10.0" :scope "test"]
                 [funcool/promesa "5.0.0"]]

  :profiles
  {:dev
   {:plugins [[lein-ancient "0.6.15"]]}})
