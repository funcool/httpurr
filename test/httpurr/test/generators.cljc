(ns httpurr.test.generators
  (:require
   [clojure.test.check.generators :as gen]
   [httpurr.status :as http]))

(defn gen-statuses
  [coll]
  (gen/such-that
   #(not (empty? %)) (gen/map (gen/return :status)
                              (gen/elements coll))))

(def informational-response
  (gen-statuses http/informational-codes))

(def success-response
  (gen-statuses http/success-codes))

(def redirection-response
  (gen-statuses http/redirection-codes))

(def client-error-response
  (gen-statuses http/client-error-codes))

(def server-error-response
  (gen-statuses http/server-error-codes))

(def error-response
  (gen-statuses (concat http/client-error-codes
                        http/server-error-codes)))
