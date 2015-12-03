(defproject funcool/httpurr "0.2.0-SNAPSHOT"
  :description "A ring-inspired, promise-returning, simple ClojureScript HTTP client."
  :url "http://funcool.github.io/httpurr"
  :license {:name "Public Domain"
            :url "http://unlicense.org"}

  :dependencies [[org.clojure/clojurescript "1.7.189" :scope "provided"]
                 [funcool/cats "1.2.0"]
                 [funcool/promesa "0.6.0-SNAPSHOT"]]

  :profiles {
    :dev {
      :dependencies [[org.clojure/test.check "0.9.0"]]
    }
  }

  :source-paths ["src"])
