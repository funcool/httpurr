(ns httpurr.test.runner
  (:require [cljs.test :as test]
            [httpurr.test.test-xhr-client]
            [httpurr.test.test-node-client]
            [httpurr.test.test-status]))

(enable-console-print!)

(defmethod test/report [:cljs.test/default :end-run-tests]
  [m]
  (if (test/successful? m)
    (.exit js/process) 0)
    (.exit js/process) 1)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'httpurr.test.test-xhr-client
                  'httpurr.test.test-node-client
                  'httpurr.test.test-status))

(set! *main-cli-fn* main)
