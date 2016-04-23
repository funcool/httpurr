(ns httpurr.client.aleph
  (:refer-clojure :exclude [get])
  (:require [aleph.http :as http]
            [manifold.deferred :as dfd]
            [httpurr.client :as c]
            [httpurr.protocols :as p]
            [httpurr.status :as s]))

;; --- Client Impl.

(defn- response?
  "Check if data has valid response like format."
  [data]
  (and (map? data)
       (s/status-code? (:status data 0))))

(defn- deferred->request
  "Coerces the aleph deferred to the Request httpur
  abstraction."
  [d]
  (letfn [(success [rsp]
            (reify
              p/Response
              (-success? [_] true)
              (-response [_] rsp)))

          (error [rsp]
            (let [data (ex-data rsp)]
              (if (response? data)
                (success data)
                (reify
                  p/Response
                  (-success? [_] false)
                  (-error [_] rsp)))))]
    (reify
      p/Request
      (-listen [_ cb]
        (dfd/on-realized d (comp cb success) (comp cb error))))))

(defn- make-uri
  [url query-string]
  (if (not query-string)
    url
    (let [idx (.indexOf url "?")]
      (if (>= idx 0)
        (str url "&" query-string)
        (str url "?" query-string)))))

(def client
  "A singleton instance of aleph client."
  (reify p/Client
    (-send [_ request {:keys [timeout] :as options}]
      (let [url (make-uri (:url request) (:query-string request))
            params (merge request
                          {:url url}
                          (when timeout
                            {:request-timeout timeout}))]
        (-> (http/request params)
            (deferred->request))))))

;; --- Shortcuts

(def send! (partial c/send! client))
(def head (partial (c/method :head) client))
(def options (partial (c/method :options) client))
(def get (partial (c/method :get) client))
(def post (partial (c/method :post) client))
(def put (partial (c/method :put) client))
(def patch (partial (c/method :patch) client))
(def delete (partial (c/method :delete) client))
(def trace (partial (c/method :trace) client))
