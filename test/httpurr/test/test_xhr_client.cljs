(ns httpurr.test.test-xhr-client
  (:require [cljs.test :as t]
            [httpurr.client :as http]
            [httpurr.client.xhr :as xhr]
            [promesa.core :as p])
  (:import goog.testing.net.XhrIo))

;; --- helpers

(defn raw-last-request
  []
  (aget (.getSendInstances XhrIo) 0))

(defn last-request
  []
  (let [r (raw-last-request)]
    {:method  (.getLastMethod r)
     :url     (.toString (.getLastUri r))
     :headers (xhr/normalize-headers
               (js->clj (.getLastRequestHeaders r)))
     :body    (.getLastContent r)}))

(defn cleanup
  []
  (.cleanup goog.testing.net.XhrIo))

(defn send!
  [& args]
  (binding [xhr/*xhr-impl* goog.testing.net.XhrIo]
    (apply http/send! xhr/client args)))

(t/use-fixtures :each
  {:after #(cleanup)})

;; --- tests

(t/deftest send-plain-get
  (let [url "http://localhost/test"
        req {:method :get
             :url url
             :headers {}}]

    (send! req)

    (let [lreq (last-request)]
      (t/is (= (:method lreq) "GET"))
      (t/is (= (:url lreq) url))
      (t/is (empty? (:headers lreq))))))

(t/deftest send-plain-get-with-query-string
  (let [url "http://localhost/test"
        query "foo=bar&baz=bar"
        url-with-query (str url "?" query)
        req {:method :get
             :query-string query
             :url url
             :headers {}}]

    (send! req)

    (let [lreq (last-request)]
      (t/is (= (:method lreq) "GET"))
      (t/is (= (:url lreq) url-with-query))
      (t/is (empty? (:headers lreq))))))

(t/deftest send-plain-get-with-encoded-query-string
  (let [url "http://localhost/test"
        query "foo=b  az"
        url-with-query (js/encodeURI (str url "?" query))
        req {:method :get
             :query-string query
             :url url
             :headers {}}]

    (send! req)

    (let [lreq (last-request)]
      (t/is (= (:method lreq) "GET"))
      (t/is (= (:url lreq) url-with-query))
      (t/is (empty? (:headers lreq))))))

(t/deftest send-plain-get-with-encoded-query-params
  (let [url "http://localhost/test"
        query {:foo ["bar" "ba z"]}
        url-with-query (str url "?" "foo=bar&foo=ba%20z")
        req {:method :get
             :query-params query
             :url url}]

    (send! req)

    (let [lreq (last-request)]
      (t/is (= (:method lreq) "GET"))
      (t/is (= (:url lreq) url-with-query)))))

(t/deftest send-plain-get-with-multiple-custom-headers
  (let [url "http://localhost/funcool/promesa"
        req {:method :get
             :url url
             :headers {"content-length" 42
                       "content-encoding" "gzip"}}]
    (send! req)

    (let [lreq (last-request)]
      (t/is (= (:method lreq) "GET"))
      (t/is (= (:url lreq) url))
      (t/is (= (:headers lreq) (:headers req))))))

(t/deftest send-request-with-body
  (let [url "http://localhost/funcool/promesa"
        content "yada yada yada"
        ctype "text/plain"
        req {:method :post
             :url url
             :headers {"content-length" 42
                       "content-encoding" "gzip"}
             :body content}]
    (send! req)
    (let [lreq (last-request)]
      (t/is (= (:method lreq) "POST"))
      (t/is (= (:url lreq) url))
      (t/is (= (:body lreq content)))
      (t/is (= (:headers lreq) (:headers req))))))

(t/deftest send-returns-a-promise
  (let [url "http://localhost/test"
        req {:method :get
             :url url
             :headers {}}
        resp (send! req)]
    (t/is (p/promise? resp))))

(t/deftest send-returns-response-map-on-success
  (t/async done
    (let [url "http://localhost/test"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (p/then resp (fn [{:keys [status body headers]}]
                     (t/is (= 200 status))
                     (t/is (empty? body))
                     (t/is (empty? headers))
                     (done))))
      (let [[xhr] (.getSendInstances goog.testing.net.XhrIo)
            status 200]
        (.simulateResponse xhr status))))

(t/deftest body-and-headers-in-response-with-error
  (t/async done
    (let [url "http://localhost/test"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (p/then resp (fn [{:keys [status body headers]}]
                      (t/is (= status 400))
                      (t/is (= body "blablala"))
                      (t/is (= headers {"content-type" "text/plain"}))
                      (done))))
      (let [xhr (raw-last-request)
            status 400
            body "blablala"
            headers #js {"content-type" "text/plain"}]
        (.simulateResponse xhr status body headers))))

(t/deftest body-and-headers-in-response
  (t/async done
    (let [url "http://localhost/test"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (p/then resp (fn [{:keys [status body headers]}]
                     (t/is (= status 200))
                     (t/is (= body "blablala"))
                     (t/is (= headers {"content-type" "text/plain"}))
                     (done))))
      (let [xhr (raw-last-request)
            status 200
            body "blablala"
            headers #js {"content-type" "text/plain"}]
        (.simulateResponse xhr status body headers))))

(t/deftest send-request-fails-when-timeout-forced
  (t/async done
    (let [url "http://localhost/test"
          req {:method :get :url url :headers {}}
          resp (send! req)]
      (p/catch resp (fn [err]
                      (t/is (instance? cljs.core.ExceptionInfo err))
                      (t/is (= (ex-data err) {:type :timeout}))
                      (done)))
      (let [xhr (raw-last-request)]
        (.simulateTimeout xhr)))))

(t/deftest request-can-be-aborted
  (t/async done
    (let [url "http://localhost/test"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (-> (p/cancel! resp)
          (p/catch (fn [res]
                     (done)))))))
