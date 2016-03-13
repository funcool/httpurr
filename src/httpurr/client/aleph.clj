(ns httpurr.client.aleph
  (:refer-clojure :exclude [get])
  (:require [aleph.http :as http]
            [manifold.deferred :as dfd]
            [httpurr.client :as c]
            [httpurr.protocols :as p]
            [httpurr.status :as s]))

(defn- success
  [response]
  (reify
    p/Response
    (-success? [_] true)
    (-response [_] response)))

(defn- error
  [err]
  (reify
    p/Response
    (-success? [_] false)
    (-error [_] err)))

(defn deferred->http
  [d]
  (reify
    p/Request
    (-listen [_ cb]
      (dfd/on-realized d
                       (comp cb success)
                       (comp cb error)))))

(def client
  (reify p/Client
    (-send [_ request {:keys [timeout] :as options}]
      (let [url (c/make-uri (:url request) (:query-string request))]
        (deferred->http (http/request (merge request {:url url
                                                      :request-timeout timeout})))))))

(def send! (partial c/send! client))
(def head (partial (c/method :head) client))
(def options (partial (c/method :options) client))
(def get (partial (c/method :get) client))
(def post (partial (c/method :post) client))
(def put (partial (c/method :put) client))
(def patch (partial (c/method :patch) client))
(def delete (partial (c/method :delete) client))
(def trace (partial (c/method :trace) client))
