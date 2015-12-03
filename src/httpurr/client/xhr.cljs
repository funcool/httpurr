(ns httpurr.client.xhr
  (:refer-clojure :exclude [get])
  (:require [httpurr.client :as c]
            [httpurr.client.util :as util]
            [httpurr.protocols :as p]
            [httpurr.errors :as e]
            [goog.events :as events])
  (:import goog.net.ErrorCode
           goog.net.EventType
           goog.net.XhrIo))

(def ^:dynamic *xhr-impl* XhrIo)

(deftype Xhr [xhr]
  p/Request
  (-listen [_ cb]
    (events/listen xhr EventType.COMPLETE
                   (fn [ev]
                     (cb (Xhr. xhr)))))

  p/Abort
  (-abort [_]
    (.abort xhr))

  p/Response
  (-success? [_]
    (.isSuccess xhr))

  (-response [_]
    {:status  (.getStatus xhr)
     :body    (.getResponse xhr)
     :headers (js->clj (.getResponseHeaders xhr))})

  (-error [_]
    (condp = (.getLastErrorCode xhr)
      ErrorCode.TIMEOUT    e/timeout
      ErrorCode.EXCEPTION  e/exception
      ErrorCode.HTTP_ERROR e/http-error
      ErrorCode.ABORT      e/abort)))

(def client
  (reify p/Client
    (-send [_ request {timeout :timeout :or {timeout 0} :as options}]
      (let [{:keys [method url headers body]} request
            method (c/keyword->method method)
            headers (util/prepare-headers headers)
            xhr (.send *xhr-impl* url nil method body headers timeout)]
        (Xhr. xhr)))))

(def send! (partial c/send! client))
(def head (partial (c/method :head) client))
(def options (partial (c/method :options) client))
(def get (partial (c/method :get) client))
(def post (partial (c/method :post) client))
(def put (partial (c/method :put) client))
(def patch (partial (c/method :patch) client))
(def delete (partial (c/method :delete) client))
(def trace (partial (c/method :trace) client))
