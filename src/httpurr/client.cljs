(ns httpurr.client
  "The HTTP client. This namespace provides a low-level `send!` primitive for
  performing requests as well as aliases for all the HTTP methods."
  (:require
   [promesa.core :as p]
   [goog.events :refer [listen]])
  (:import
   [goog Uri]
   [goog.net XhrIo]
   [goog.net ErrorCode]
   [goog.net EventType])
  (:refer-clojure :exclude [get]))

(def keyword->method
  {:head    "HEAD"
   :options "OPTIONS"
   :get     "GET"
   :post    "POST"
   :put     "PUT"
   :patch   "PATCH"
   :delete  "DELETE"
   :trace   "TRACE"})

(defn request->headers
  [{:keys [headers]}]
  (let [h (or headers {})]
    (if (empty? h)
      #js {}
      (clj->js h))))

(defn- perform!
  [request {timeout :timeout :or {timeout 0} :as options}]
  (let [{:keys [method
                url
                headers
                body
                query-string]} request
         uri (Uri. url)
         uri (if query-string
               (.setQuery uri query-string)
               uri)
         method (keyword->method method)
         headers (request->headers request)]
    (.send XhrIo
           uri
           nil
           method
           body
           headers
           timeout)))

(defn xhr->response
  [xhr]
  {:pre [(.isSuccess xhr)]}
  {:status  (.getStatus xhr)
   :body    (.getResponse xhr)
   :headers (js->clj (.getResponseHeaders xhr))})

(defn xhr->error
  [xhr]
  {:pre [(not (.isSuccess xhr))]}
  (condp = (.getLastErrorCode xhr)
    ErrorCode.TIMEOUT    :timeout
    ErrorCode.EXCEPTION  :exception
    ErrorCode.HTTP_ERROR :http-error
    ErrorCode.ABORT      :abort))

(defn xhr->promise
  "Given a XHR object return a promise that will be resolved
  if there is a response and rejected on timeout, exceptions,
  HTTP errors or aborts."
  [xhr]
  (p/promise (fn [resolve reject]
               (listen xhr
                       EventType.COMPLETE
                       (fn [ev]
                         (let [xhr (.-target ev)]
                           (if (.isSuccess xhr)
                             (resolve (xhr->response xhr))
                             (reject (xhr->error xhr)))))))))

(defn send!
  "Given a request map and maybe an options map, perform
  the request and return a promise that will be resolved
  when receiving the response.

  If the request timeouts, throws an exception or is aborted
  the promise will be rejected.

  The available options are:
     - `:timeout`: a timeout for the request in miliseconds
  "
  ([request]
   (send! request {}))
  ([request options]
   (let [xhr (perform! request options)
         p (xhr->promise xhr)]
     (-> (.cancellable p)
         (p/catch js/Promise.CancellationError
                  (fn []
                    (.abort xhr)
                    (throw :abort)))))))

(defn abort!
  "Given a promise resulting from a request, make a best-effort
  to abort the operation if the promise is still pending."
  [p]
  (.cancel p))

;; facade

(defn- method
  [m]
  (fn
    ([url]
     (send! {:method m :url url}))
    ([url req]
     (send! (merge req {:method m :url url})))
    ([url req opts]
     (send! (merge req {:method m :url url}) opts))))

(def head    (method :head))
(def options (method :options))
(def get     (method :get))
(def post    (method :post))
(def put     (method :put))
(def patch   (method :patch))
(def delete  (method :delete))
(def trace   (method :trace))
