(ns httpurr.test.runner
  (:require
    [cljs.test :as test]
    [doo.runner :refer-macros [doo-tests]]
    [httpurr.test.test-node-client]
    [httpurr.test.test-status]
    [httpurr.test.test-xhr-client]))

(doo-tests 'httpurr.test.test-node-client
           'httpurr.test.test-status)
