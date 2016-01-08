(defproject funcool/httpurr "0.2.0"
  :description "A ring-inspired, promise-returning, simple ClojureScript HTTP client."
  :url "http://funcool.github.io/httpurr"
  :license {:name "Public Domain" :url "http://unlicense.org"}

  :dependencies [[org.clojure/clojure "1.7.0" :scope "provided"]
                 [org.clojure/clojurescript "1.7.189" :scope "provided"]
                 [org.clojure/test.check "0.9.0" :scope "test"]
                 [funcool/cats "1.2.1"]
                 [funcool/promesa "0.7.0"]]

  :plugins [[lein-ancient "0.6.7"]]
  :source-paths ["src"])
