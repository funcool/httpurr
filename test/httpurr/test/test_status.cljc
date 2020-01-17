(ns httpurr.test.test-status
  (:require
   #?(:clj  [clojure.test :as t]
      :cljs [cljs.test :as t])
   #?(:clj [clojure.test.check.clojure-test :refer [defspec]]
      :cljs [clojure.test.check.clojure-test :refer-macros [defspec]])
   [httpurr.test.generators :as gen]
   [httpurr.status :as http]
   ;; [clojure.test.check :as tc]
   #?(:clj  [clojure.test.check.properties :as props]
      :cljs [clojure.test.check.properties :as props :include-macros true])))

;; 1xx
(defspec informational?
  100
  (props/for-all
   [response gen/informational-response]
   (t/is (http/informational? response))))

;; 2xx
(defspec success?
  100
  (props/for-all
   [response gen/success-response]
   (t/is (http/success? response))))

;; 3xx
(defspec redirection?
  100
  (props/for-all
   [response gen/redirection-response]
   (t/is (http/redirection? response))))

;; 4xx
(defspec client-error?
  100
  (props/for-all
   [response gen/client-error-response]
   (t/is (http/client-error? response))))

;; 5xx
(defspec server-error?
  100
  (props/for-all
   [response gen/server-error-response]
   (t/is (http/server-error? response))))

;; 4-5xx
(defspec error?
  100
  (props/for-all
   [response gen/error-response]
   (t/is (http/error? response))))
