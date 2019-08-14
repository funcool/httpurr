(ns httpurr.client.clj-http-lite
  (:refer-clojure :exclude [get])
  (:require [clj-http.lite.client :as http]
            [httpurr.client :as c]
            [promesa.core :as fp]
            [httpurr.protocols :as p]
            [httpurr.status :as s])
  (:import [java.net URI]))

;; --- Client Impl.

(defn- response?
  "Check if data has valid response like format."
  [data]
  (and (map? data)
       (s/status-code? (:status data 0))))

(deftype HttpResponse [http-response]
  p/Response
  (-success? [_]
    true)
  (-response [_]
    http-response))

(deftype HttpErrorResponse [error]
  p/Response
  (-success? [_]
    false)
  (-error [_]
    error))

(deftype HttpRequest [future-response]
  p/Request
  (-listen [_ callback]
    (callback
      (try
        (let [response @future-response]
          (HttpResponse. response))
        (catch Throwable e
          (let [data (or (ex-data e) (ex-data (.getCause e)))]
            (if (response? data)
              (HttpResponse. data)
              (HttpErrorResponse. e))))))))

(defn- make-uri
  [url query-string]
  (if (not query-string)
    url
    (let [^URI uri (URI. url)
          idx (.indexOf url "?")
          ^String query (if (>= idx 0)
                          (str (.getQuery uri) "&" query-string)
                          query-string)
          ^URI uri-w-query (URI. (.getScheme uri)
                                 (.getUserInfo uri)
                                 (.getHost uri)
                                 (.getPort uri)
                                 (.getPath uri)
                                 query   
                                 (.getFragment uri))]
      (.toASCIIString uri-w-query))))

(deftype HttpClient [default-options]
  p/Client
  (-send [_ request request-options]
    (let [{:keys [timeout] :as options} (merge default-options request-options)
          url (make-uri (:url request) (:query-string request))
          params (merge request
                        options
                        {:url url}
                        (when timeout
                          {:conn-timeout timeout
                           :socket-timeout timeout}))]
      (-> (future (http/request params))
          (HttpRequest.)))))

(defn make-http-client
  [& {:as default-options}]
  (HttpClient. default-options))

(def client (make-http-client :as :stream :throw-exceptions false))

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
