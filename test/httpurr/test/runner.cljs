(ns httpurr.test.runner
  (:require [cljs.test :as test]
            [httpurr.test.test-xhr-client]
            [httpurr.test.test-status]))

(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'httpurr.test.test-xhr-client
                  'httpurr.test.test-status))

(set! *main-cli-fn* main)
