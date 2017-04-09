(ns httpurr.test.test-node-client
  (:require [cljs.test :as t]
            [cljs.nodejs :as node]
            [httpurr.client :as http]
            [httpurr.client.node :as a]
            [promesa.core :as p]))

;; --- helpers

(def ^:private last-request (atom nil))

(defn- send!
  [request & args]
  (apply http/send! a/client request args))

(def ^:private http (node/require "http"))
(def ^:private url (node/require "url"))
(def ^:const port 44556)

(defn- read-request
  [request]
  (let [opts (.parse url (.-url request))]
    {:query (.-query opts)
     :path (.-pathname opts)
     :method (keyword (.toLowerCase (.-method request)))
     :headers (js->clj (.-headers request))}))

(def server
  (letfn [(handler [request response]
            (reset! last-request (read-request request))
            (case (.-url request)
              "/error400"
              (do
                (.writeHead response 400 #js {"content-type" "text/plain"})
                (.end response "hello world"))

              "/error500"
              (do
                (.writeHead response 500 #js {"content-type" "text/plain"})
                (.end response "hello world"))

              "/timeout"
              (do
                (js/setTimeout (fn []
                                 (.writeHead response 500 #js {"content-type" "text/plain"})
                                 (.end response "hello world"))
                               1000))

              "/chunked"
              (do
                (.writeHead response 200 #js {"content-type" "text/plain"})
                (doseq [x (range 2500)]
                  (.write response (str "this is line number " x ", ")))
                (.write response "\n")
                (.end response "world"))

              (do
                (.writeHead response 200 #js {"content-type" "text/plain"})
                (.end response "hello world"))))]
    (-> (.createServer http handler)
        (.listen port "0.0.0.0"))))

(defn- make-uri
  [path]
  (str "http://127.0.0.1:" port path))

;; --- tests

(t/deftest send-plain-get
  (t/async done
    (let [path "/test"
          uri (make-uri path)
          req {:method :get
               :url uri
               :headers {}}]

      (p/then (send! req)
              (fn [response]
                ;; (js/console.log "response")
                (let [lreq @last-request]
                  (t/is (= (:method lreq) :get))
                  (t/is (= (:path lreq) path))
                  (done)))))))

;
(t/deftest send-chunked
  (t/async done
    (let [path "/chunked"
          uri (make-uri path)
          req {:method :get
               :url uri
               :headers {}}]

      (p/then (send! req)
              (fn [response]
                (t/is (= (count (.split (str (:body response)) ","))
                         2501))
                (let [lreq @last-request]
                  (t/is (= (:method lreq) :get))
                  (t/is (= (:path lreq) path))
                  (done)))))))

(t/deftest send-plain-get-with-query-string
  (t/async done
    (let [path "/test"
          url (make-uri path)
          query "foo=bar&baz=frob"
          req {:method :get
               :query-string query
               :url url}]
      (p/then (send! req)
              (fn [response]
                (let [lreq @last-request]
                  (t/is (= (:method lreq) :get))
                  (t/is (= (:path lreq) path))
                  (t/is (= (:query lreq) query))
                  (done)))))))

(t/deftest send-plain-get-with-encoded-query-params
  (t/async done
    (let [path "/test"
          url (make-uri path)
          query {:foo ["bar" "ba z"]}
          req {:method :get
               :query-params query
               :url url}]

      (p/then (send! req)
              (fn [response]
                (let [lreq @last-request]
                  (t/is (= (:method lreq) :get))
                  (t/is (= (:path lreq) path))
                  (t/is (:query lreq) "foo=bar&foo=ba%20z")
                  (done)))))))

(t/deftest send-plain-get-with-query-string-on-path
  (t/async done
    (let [path "/test"
          url (make-uri path)
          query "foo=bar&baz=frob"

          req {:method :get
               :url (str url "?" query)}]
      (p/then (send! req)
              (fn [response]
                (let [lreq @last-request]
                  (t/is (= (:method lreq) :get))
                  (t/is (= (:path lreq) path))
                  (t/is (= (:query lreq) query))
                  (done)))))))

(t/deftest send-plain-get-with-custom-header
  (t/async done
    (let [path "/test2"
          url (make-uri path)
          req {:method :get
               :url url
               :headers {"Content-Type" "application/json"}}]

      (p/then (send! req)
              (fn [response]
                (let [lreq @last-request]
                  (t/is (= (:method lreq) :get))
                  (t/is (= (:path lreq) path))
                  (t/is (= (get-in lreq [:headers "content-type"])
                           "application/json"))
                  (done)))))))

(t/deftest send-returns-response-map-on-success
  (t/async done
    (let [path "/test"
          url (make-uri path)
          req {:method :get
               :url url}]
      (p/then (send! req)
              (fn [resp]
                (t/is (= 200 (:status resp)))
                (t/is (= "hello world" (str (:body resp))))
                (done))))))

(t/deftest send-returns-response-map-on-failure-400
  (t/async done
    (let [path "/error400"
          url (make-uri path)
          req {:method :get
               :url url}]
      (p/then (send! req)
              (fn [resp]
                (t/is (= 400 (:status resp)))
                (t/is (= "hello world" (str (:body resp))))
                (done))))))

(t/deftest send-returns-response-map-on-failure-500
  (t/async done
    (let [path "/error500"
          url (make-uri path)
          req {:method :get
               :url url}]
      (p/then (send! req)
              (fn [resp]
                (t/is (= 500 (:status resp)))
                (t/is (= "hello world" (str (:body resp))))
                (done))))))

(t/deftest request-can-be-aborted
  (t/async done
    (let [path "/timeout"
          url (make-uri path)
          req {:method :get
               :url url
               :headers {}}
          resp (send! req)]
      (-> (p/cancel! resp)
          (p/catch (fn [res]
                     (done)))))))

(t/deftest request-timeout
  (t/async done
    (let [path "/timeout"
          url (make-uri path)
          req {:method :get
               :url url
               :headers {}}
          resp (send! req {:timeout 400})]
      (p/catch resp (fn [response]
                      (t/is (instance? cljs.core.ExceptionInfo response))
                      (t/is (= (ex-data response) {:type :timeout}))
                      (done))))))
