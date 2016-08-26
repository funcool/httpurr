(ns httpurr.client.xhr
  (:refer-clojure :exclude [get])
  (:require [httpurr.client :as c]
            [httpurr.protocols :as p]
            [goog.events :as events]
            [clojure.string :as str])
  (:import [goog.net ErrorCode EventType]
           [goog.net XhrIo]
           [goog.Uri QueryData]
           [goog Uri]))

(def ^:dynamic *xhr-impl* XhrIo)

(defn normalize-headers
  [headers]
  (reduce-kv (fn [acc k v]
               (assoc acc (str/lower-case k) v))
             {} headers))

(defn- translate-error-code
  [code]
  (condp = code
    ErrorCode.TIMEOUT    :timeout
    ErrorCode.EXCEPTION  :exception
    ErrorCode.HTTP_ERROR :http
    ErrorCode.ABORT      :abort))

(deftype Xhr [xhr]
  p/Request
  (-listen [_ cb]
    (events/listen xhr EventType.COMPLETE #(cb (Xhr. xhr))))

  p/Abort
  (-abort [_]
    (.abort xhr))

  p/Response
  (-success? [_]
    (or (.isSuccess xhr)
        (let [code (.getLastErrorCode xhr)]
          (= code ErrorCode.HTTP_ERROR))))

  (-response [_]
    {:status  (.getStatus xhr)
     :body    (.getResponse xhr)
     :headers (-> (.getResponseHeaders xhr)
                  (js->clj)
                  (normalize-headers))})

  (-error [this]
    (let [type (-> (.getLastErrorCode xhr)
                   (translate-error-code))
          message (.getLastError xhr)]
      (ex-info message {:type type}))))

(defn- make-uri
  [url qs qp]
  (let [uri (Uri. url)]
    (when qs (.setQuery uri qs))
    (when qp
      (let [dt (.createFromMap QueryData (clj->js  qp))]
        (.setQueryData uri dt)))
    (.toString uri)))

(def client
  (reify p/Client
    (-send [_ request options]
      (let [{:keys [timeout with-credentials?] :or {timeout 0 with-credentials? false}} options
            {:keys [method url query-string query-params headers body]} request
            uri (make-uri url query-string query-params)
            method (c/keyword->method method)
            headers (if headers (clj->js headers) #js {})
            xhr (.send *xhr-impl* uri nil method body headers timeout with-credentials?)]
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
