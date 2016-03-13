(ns httpurr.test.test-errors
  (:require
   #?(:clj
      [clojure.test :as t]
      :cljs
      [cljs.test :as t])
   [httpurr.errors :as e]))

(t/deftest timeout
  (t/is (e/timeout? e/timeout)))

(t/deftest exception
  (t/is (e/exception? e/exception)))

(t/deftest http-error
  (t/is (e/http-error? e/http-error)))

(t/deftest abort
  (t/is (e/abort? e/abort)))
