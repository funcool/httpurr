(ns httpurr.test.test-status
  (:require #?(:clj [clojure.test :as t] :cljs [cljs.test :as t])
            #?(:clj [clojure.test.check.clojure-test :refer [defspec]])
            [httpurr.test.generators :as gen]
            [httpurr.status :as http]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop :include-macros true])
  #?(:cljs (:require-macros [clojure.test.check.clojure-test :refer [defspec]])))

;; 1xx
(defspec informational?
  100
  (prop/for-all [response gen/informational-response]
    (http/informational? response)))

;; 2xx
(defspec success?
  100
  (prop/for-all [response gen/success-response]
    (http/success? response)))

;; 3xx
(defspec redirection?
  100
  (prop/for-all [response gen/redirection-response]
    (http/redirection? response)))

;; 4xx
(defspec client-error?
  100
  (prop/for-all [response gen/client-error-response]
    (http/client-error? response)))

;; 5xx
(defspec server-error?
  100
  (prop/for-all [response gen/server-error-response]
    (http/server-error? response)))

;; 4-5xx
(defspec error?
  100
  (prop/for-all [response gen/error-response]
    (http/error? response)))
