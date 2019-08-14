(ns httpurr.test.test-clj-http-lite-client
  (:require [clojure.test :as t]
            [byte-streams :as bs]
            [httpurr.client :as http]
            [httpurr.client.clj-http-lite :refer [client]]
            [aleph.http :as ahttp]
            [promesa.core :as p]))

;; --- helpers

(def ^:private last-request (atom nil))

(defn- send!
  [request]
  (http/send! client request))

(defn- read-request
  [{:keys [request-method headers uri body query-string]}]
  {:body (when body (bs/to-string body))
   :query query-string
   :method request-method
   :headers headers
   :path uri})

(defn- test-handler
  [{:keys [body] :as request}]
  (let [request (merge request (when body {:body (bs/to-string body)}))]
    (reset! last-request (read-request request))
    (if (= (:body request) "error")
      {:status 400
       :body (:body request)
       :content-type "text/plain"}
      {:status 200
       :body (:body request)
       :content-type "text/plain"})))

(def ^:private port (atom 0))

(defonce ^:private server
  (ahttp/start-server test-handler {:port 0}))

(reset! port (.port server))

(defn- make-uri
  [path]
  (let [p @port]
    (str "http://localhost:" p path)))

;; --- tests

(t/deftest send-plain-get
  (let [path "/funcool/cats"
        uri (make-uri path)
        req {:method :get
             :url uri
             :headers {}}]

    @(send! req)

    (let [lreq @last-request]
      (t/is (= (:method lreq) :get))
      (t/is (= (:path lreq) path)))))

(t/deftest send-plain-get-with-query-string
  (let [path "/funcool/cats"
        url (make-uri path)
        query "foo=bar&baz=frob"
        req {:method :get
             :query-string query
             :url url}]

    @(send! req)

    (let [lreq @last-request]
      (t/is (= (:method lreq) :get))
      (t/is (= (:path lreq) path))
      (t/is (= (:query lreq) query)))))

(t/deftest send-plain-get-with-encoded-query-string
  (let [path "/funcool/cats"
        url (make-uri path)
        query "foo=b az"
        req {:method :get
             :query-string query
             :url url}]

    @(send! req)

    (let [lreq @last-request]
      (t/is (= (:method lreq) :get))
      (t/is (= (:path lreq) path))
      (t/is (= (:query lreq) (.replaceAll query " " "%20"))))))

(t/deftest send-plain-get-with-encoded-query-params
  (let [path "/funcool/cats"
        url (make-uri path)
        query {:foo ["bar" "ba z"]}
        req {:method :get
             :query-params query
             :url url}]

    @(send! req)

    (let [lreq @last-request]
      (t/is (= (:method lreq) :get))
      (t/is (= (:path lreq) path))
      (t/is (= (:query lreq) "foo=bar&foo=ba+z")))))

(t/deftest send-plain-get-with-custom-header
  (let [path "/funcool/cats"
        url (make-uri path)
        req {:method :get
             :url url
             :headers {"Content-Type" "application/json"}}]

    @(send! req)

    (let [lreq @last-request]
      (t/is (= (:method lreq) :get))
      (t/is (= (:path lreq) path))
      (t/is (= (get-in lreq [:headers "content-type"])
               "application/json")))))

(t/deftest send-request-with-body
  (let [path "/funcool/promesa"
        url (make-uri path)
        content "yada yada yada"
        ctype "text/plain"
        req {:method :post
             :url url
             :headers {"content-type" ctype}
             :body content}]

    (let [response @(send! req)
          lreq @last-request]
      (t/is (= (:method lreq) :post))
      (t/is (= (:path lreq) path))
      (t/is (= content (slurp (:body response)))))))

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
    (let [lreq @last-request]
      (t/is (= (:method lreq) :get))
      (t/is (= (:path lreq) path))
      (t/is (= 200 (:status resp))))))

(t/deftest send-returns-response-failure
  (let [path "/funcool/cats"
        url (make-uri path)
        req {:method :post
             :body "error"
             :url url}
        resp  @(send! req)]
    (let [lreq @last-request]
      (t/is (= (:method lreq) :post))
      (t/is (= (:path lreq) path))
      (t/is (= 400 (:status resp))))))
