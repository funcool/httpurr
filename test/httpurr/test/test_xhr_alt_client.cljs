(ns httpurr.test.test-xhr-alt-client
  (:require [cljs.test :as t]
            [httpurr.client.xhr-alt :as xhr-alt]))

(t/deftest make-uri-test
  (t/testing "no question mark when no params"
    (t/is (= (xhr-alt/make-uri "foo/bar" nil nil)
             "foo/bar")))
  (t/testing "question mark when params"
    (t/is (= (xhr-alt/make-uri "foo/bar" "x=42" nil)
             "foo/bar?x=42")))
  (t/testing "params separated by &"
    (t/is (= (xhr-alt/make-uri "foo/bar" nil "x=42&y=43")
             "foo/bar?x=42&y=43")))
  (t/testing "both key and value will be encoded"
    (t/is (= (xhr-alt/make-uri "foo/bar" "x%=%42" nil)
             "foo/bar?x%25=%2542")))
  (t/testing "keywords in keys will be converted to strings"
    (t/is (= (xhr-alt/make-uri "foo/bar" nil {:baz 43})
             "foo/bar?baz=43")))
  (t/testing "query params win over query string"
    (t/is (= (xhr-alt/make-uri "foo/bar" "baz=42" {"baz" "43"})
             "foo/bar?baz=43"))))