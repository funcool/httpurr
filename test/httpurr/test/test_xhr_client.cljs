(ns httpurr.test.test-xhr-client
  (:require [cljs.test :as t]
            [httpurr.client :as http]
            [httpurr.client.xhr :as xhr]
            [promesa.core :as p])
  (:import goog.testing.net.XhrIo))

;; helpers

(defn raw-last-request
  []
  (aget (.getSendInstances XhrIo) 0))

(defn last-request
  []
  (let [r (raw-last-request)]
    {:method  (.getLastMethod r)
     :uri     (.toString (.getLastUri r))
     :headers (js->clj (.getLastRequestHeaders r))
     :body    (.getLastContent r)}))

(defn cleanup
  []
  (.cleanup goog.testing.net.XhrIo))

(defn send!
  [& args]
  (binding [xhr/*xhr-impl* goog.testing.net.XhrIo]
    (apply http/send! xhr/client args)))

;; tests

(t/use-fixtures :each
  {:after #(cleanup)})

(t/deftest send-plain-get
  (let [url "http://github.com/funcool/cats"
        req {:method :get
             :url url
             :headers {}}]

    (send! req)

    (let [{:keys [method
                  uri
                  headers]} (last-request)]
      (t/is (= method "GET"))
      (t/is (= uri url))
      (t/is (empty? headers)))))

(t/deftest send-plain-get-with-query-string
  (let [url "http://github.com/funcool/cats"
        query "foo=bar&baz=bar"
        url-with-query (str url "?" query)
        req {:method :get
             :query-string query
             :url url
             :headers {}}]

    (send! req)

    (let [{:keys [method
                  uri
                  headers]} (last-request)]
      (t/is (= method "GET"))
      (t/is (= uri url-with-query))
      (t/is (empty? headers)))))

(t/deftest send-plain-get-with-encoded-query-string
  (let [url "http://github.com/funcool/cats"
        query "foo=b  az"
        url-with-query (js/encodeURI (str url "?" query))
        req {:method :get
             :query-string query
             :url url
             :headers {}}]

    (send! req)

    (let [{:keys [method
                  uri
                  headers]} (last-request)]
      (t/is (= method "GET"))
      (t/is (= uri url-with-query))
      (t/is (empty? headers)))))

(t/deftest send-plain-get-with-custom-header
  (let [url "http://www.github.com/funcool/promesa"
        req {:method :get
             :url url
             :headers {"Content-Type" "application/json"}}]

    (send! req)

    (let [{:keys [method
                  uri
                  headers]} (last-request)]
      (t/is (= method "GET"))
      (t/is (= uri url))
      (t/is (= headers (:headers req))))))

(t/deftest send-plain-get-with-multiple-custom-headers
  (let [url "http://www.github.com/funcool/promesa"
        req {:method :get
             :url url
             :headers {"Content-Length" 42
                       "Content-Encoding" "gzip"}}]
    (send! req)

    (let [{:keys [headers]} (last-request)]
      (t/is (= headers (:headers req))))))

(t/deftest send-request-with-body
  (let [url "http://www.github.com/funcool/promesa"
        content "yada yada yada"
        ctype "text/plain"
        req {:method :post
             :url url
             :headers {"Content-Length" 42
                       "Content-Encoding" "gzip"}
             :body content}]
    (send! req)
    (let [{:keys [method
                  body
                  headers]} (last-request)]
      (t/is (= method "POST"))
      (t/is (= body content))
      (t/is (= headers (:headers req))))))

;; responses

(t/deftest send-returns-a-promise
  (let [url "http://www.github.com/funcool/cats"
        req {:method :get
             :url url
             :headers {}}
        resp (send! req)]
    (t/is (p/promise? resp))))

(t/deftest send-returns-response-map-on-success
  (t/async done
    (let [url "http://www.github.com/funcool/cats"
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
    (let [url "http://www.github.com/funcool/cats"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (p/then resp (fn [{:keys [status body headers]}]
                      (t/is (= status 400))
                      (t/is (= body "blablala"))
                      (t/is (= headers {"Content-Type" "text/plain"}))
                      (done))))
      (let [xhr (raw-last-request)
            status 400
            body "blablala"
            headers #js {"Content-Type" "text/plain"}]
        (.simulateResponse xhr status body headers))))

(t/deftest body-and-headers-in-response
  (t/async done
    (let [url "http://www.github.com/funcool/cats"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (p/then resp (fn [{:keys [status body headers]}]
                     (t/is (= status 200))
                     (t/is (= body "blablala"))
                     (t/is (= headers {"Content-Type" "text/plain"}))
                     (done))))
      (let [xhr (raw-last-request)
            status 200
            body "blablala"
            headers #js {"Content-Type" "text/plain"}]
        (.simulateResponse xhr status body headers))))

;; note: XhrIo mock doesn't respect timeouts
#_(t/deftest send-request-fails-if-expired-timeout
  (t/async done
    (let [url "http://www.github.com/funcool/cats"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req {:timeout 1})]
      (p/catch resp (fn [err]
                      (t/is (= err e/timeout))
                      (done))))))

(t/deftest send-request-fails-when-timeout-forced
  (t/async done
    (let [url "http://www.github.com/funcool/cats"
          req {:method :get :url url :headers {}}
          resp (send! req)]
      (p/catch resp (fn [err]
                      (t/is (= err :timeout))
                      (done)))
      (let [xhr (raw-last-request)]
        (.simulateTimeout xhr)))))

(t/deftest request-can-be-aborted
  (t/async done
    (let [url "http://www.github.com/funcool/cats"
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (p/finally resp (fn [response]
                        (t/is (p/cancelled? resp))
                        (done)))
      (p/catch (p/cancel! resp) (constantly nil)))))

