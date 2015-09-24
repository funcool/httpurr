(ns httpurr.runner
  (:require [cljs.test :as test]
            [httpurr.test.client-test]
            [httpurr.test.errors-test]
            [httpurr.test.status-test]))

(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'httpurr.test.client-test
                  'httpurr.test.errors-test
                  'httpurr.test.status-test))

(set! *main-cli-fn* main)
