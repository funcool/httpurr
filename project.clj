(defproject funcool/httpurr "0.1.1"
  :description "A simple and non obstructive http client for ClojureScript."
  :url "http://funcool.github.io/httpurr"
  :license {:name "Public Domain"
            :url "http://unlicense.org"}

  :dependencies [[org.clojure/clojurescript "1.7.145" :scope "provided"]
                 [funcool/cats "1.0.0"]
                 [funcool/promesa "0.5.1"]]

  :profiles {
    :dev {
      :dependencies [[org.clojure/test.check "0.8.2"]]
    }
  }

  :source-paths ["src"])
