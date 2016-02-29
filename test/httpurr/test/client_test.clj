(ns httpurr.test.client-test
  (:require [clojure.test :as t]
            [httpurr.client :as http]
            [httpurr.errors :as e]
            [httpurr.client.aleph :as a]
            [aleph.http :as ahttp]
            [promesa.core :as p]))

(def last-request (atom nil))

(defn send!
  [request]
  (http/send! a/client request))

(defn handler
  [request]
  (reset! last-request request)
  {:status 200
   :body (pr-str request)
   :content-type "text/plain"})

(def port (atom 0))

(defonce server
  (ahttp/start-server handler {:port 0}))

(reset! port (.port server))

(defn make-uri
  [path]
  (let [p @port]
    (str "http://localhost:" p path)))

;; tests

(t/deftest send-plain-get
  (let [path "/funcool/cats"
        uri (make-uri path)
        req {:method :get
             :url uri
             :headers {}}]

    @(send! req)

    (let [{:keys [request-method uri]} @last-request]
      (t/is (= request-method :get))
      (t/is (= uri path)))))

(t/deftest send-plain-get-with-query-string
  (let [path "/funcool/cats"
        url (make-uri path)
        query "foo=bar&baz=frob"
        url-with-query (str url "?" query)
        req {:method :get
             :query-string query
             :url url}]

    @(send! req)

    (let [{:keys [request-method
                  uri
                  query-string]} @last-request]
      (t/is (= request-method :get))
      (t/is (= uri path))
      (t/is (= query-string query)))))

(t/deftest send-plain-get-with-encoded-query-string
  (let [path "/funcool/cats"
        url (make-uri path)
        query "foo=b  az"
        req {:method :get
             :query-string query
             :url url}]

    @(send! req)

    (let [{:keys [request-method
                  uri
                  query-string]} @last-request]
      (t/is (= request-method :get))
      (t/is (= uri path))
      (t/is (= query-string (.replaceAll query " " "%20"))))))

(t/deftest send-plain-get-with-custom-header
  (let [path "/funcool/cats"
        url (make-uri path)
        req {:method :get
             :url url
             :headers {"Content-Type" "application/json"}}]

    @(send! req)

    (let [{:keys [request-method
                  uri
                  headers]} @last-request]
      (t/is (= request-method :get))
      (t/is (= uri path))
      (t/is (= (get headers "content-type")
               "application/json")))))

(t/deftest send-plain-get-with-multiple-custom-headers
  (let [path "/funcool/promesa"
        url (make-uri path)
        req {:method :get
             :url url
             :headers {"x-a-header" "42"
                       "x-another-header" "foo"}}]
    @(send! req)

    (let [{:keys [headers]} @last-request
          sent-headers (:headers req)]
      (t/is (= (select-keys headers (keys sent-headers))
               sent-headers)))))

(t/deftest send-request-with-body
  (let [path "/funcool/promesa"
        url (make-uri path)
        content "yada yada yada"
        ctype "text/plain"
        req {:method :post
             :url url
             :headers {"content-type" ctype}
             :body content}]

    @(send! req)

    (let [{:keys [request-method
                  body
                  headers]} @last-request
          sent-headers (:headers req)]
      (t/is (= request-method :post))
      #_(t/is (= (slurp body) content)))))


(t/deftest send-returns-a-promise
  (let [path "/funcool/cats"
        url (make-uri path)
        req {:method :get
             :url url}
        resp (send! req)]
    (t/is (p/promise? resp))))

(t/deftest send-returns-response-map-on-success
  (let [path "/funcool/cats"
        url (make-uri path)
        req {:method :get
             :url url}
        resp @(send! req)]
    (let [{:keys [status body headers]} resp
          request (read-string (slurp body))]
      (t/is (= 200 status))
      (t/is (= (:request-method request) :get))
      (t/is (= (:uri request) path)))))
