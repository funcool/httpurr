(ns httpurr.client.node
  (:require
   [httpurr.client :as c]
   [httpurr.client.util :refer [prepare-headers]]
   [httpurr.protocols :as p]
   [httpurr.errors :as e])
  (:refer-clojure :exclude [get]))

(def http (js/require "http"))
(def url (js/require "url"))

(defn url->options
  [u]
  (let [parsed (.parse url u)]
    {:protocol (.-protocol parsed)
     :host (.-host parsed)
     :port (.-port parsed)
     :path (.-path parsed)
     :query (.-query parsed)}))

(deftype HttpResponseError [err]
  p/Response
  (success? [_] false)
  (error [_] err))

(deftype HttpResponse [msg]
  p/Response
  (success? [_] true)

  (response [_]
    (let [headersv (partition 2 (js->clj (.-rawHeaders msg)))]
      {:status  (.-statusCode msg)
       :body    (.read msg)
       :headers (zipmap
                 (map first headersv)
                 (map second headersv))})))

(deftype HttpRequest [req]
  p/Request
  (listen! [_ cb]
    ;; ok
    (.on req
         "response"
         (fn [msg]
           (.on msg
                "readable"
                (fn [_]
                  (cb (HttpResponse. msg))))))
    ;; errors
    (.on req
         "abort"
         (fn [err]
           (cb (HttpResponseError. e/abort))))
    (.on req
         "timeout"
         (fn [err]
           (cb (HttpResponseError. e/timeout))))
    (.on req
         "clientError"
         (fn [err]
           (cb (HttpResponseError. e/http-error))))
    (.on req
         "error"
         (fn [err]
           (cb (HttpResponseError. e/exception)))))

  p/Abort
  (abort! [_]
    (.abort req)))

(def client
  (reify
    p/Client
    (send! [_ request {timeout :timeout :or {timeout 0} :as options}]
      (let [{:keys [method
                    url
                    headers
                    body]} request
            method (c/keyword->method method)
            headers (prepare-headers headers)
            options (merge {:method method
                            :headers headers}
                           (url->options url))
            req (.request http (clj->js options))]
        (.setTimeout req timeout)
        (.end req)
        (HttpRequest. req)))))

(def send!
  (partial c/send! client))

(def head
  (partial (c/method :head) client))

(def options
  (partial (c/method :options) client))

(def get
  (partial (c/method :get) client))

(def post
  (partial (c/method :post) client))

(def put
  (partial (c/method :put) client))

(def patch
  (partial (c/method :patch) client))

(def delete
  (partial (c/method :delete) client))

(def trace
  (partial (c/method :trace) client))
