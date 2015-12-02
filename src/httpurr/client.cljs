(ns httpurr.client
  "The HTTP client. This namespace provides a low-level `send!` primitive for
  performing requests as well as aliases for all the HTTP methods."
  (:require
   [promesa.core :as p]
   [httpurr.protocols :as proto])
  (:import [goog Uri])
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

(defn- perform!
  [client request options]
  (let [{:keys [method
                url
                headers
                body
                query-string]} request
         uri (Uri. url)
         uri (if query-string
               (.setQuery uri query-string)
               uri)
         request (-> (assoc request :url (.toString uri))
                     (dissoc :query-string))]
    (proto/send! client request options)))

(defn request->promise
  "Given a object that implements `httpurr.protocols.Request`,
  return a promise that will be resolved if there is a
  response and rejected on timeout, exceptions, HTTP errors
  or abortions."
  [request]
  (p/promise (fn [resolve reject on-cancel]
               (when (fn? on-cancel)
                 (on-cancel (fn []
                              (when (satisfies? proto/Abort request)
                               (proto/abort! request)))))
               (proto/listen! request
                              (fn [resp]
                                (if (proto/success? resp)
                                  (resolve (proto/response resp))
                                  (reject (proto/error resp))))))))

(defn send!
  "Given a request map and maybe an options map, perform
  the request and return a promise that will be resolved
  when receiving the response.

  If the request timeouts, throws an exception or is aborted
  the promise will be rejected.

  The available options are:
     - `:timeout`: a timeout for the request in miliseconds
  "
  ([client request]
   (send! client request {}))
  ([client request options]
   (let [request (perform! client request options)]
     (request->promise request))))

(defn abort!
  "Given a promise resulting from a request, make a best-effort
  to abort the operation if the promise is still pending."
  [p]
  (.cancel p))

;; facade

(defn method
  [m]
  (fn
    ([client url]
     (send! client {:method m :url url}))
    ([client url req]
     (send! client (merge req {:method m :url url})))
    ([client url req opts]
     (send! client (merge req {:method m :url url}) opts))))

(def head    (method :head))
(def options (method :options))
(def get     (method :get))
(def post    (method :post))
(def put     (method :put))
(def patch   (method :patch))
(def delete  (method :delete))
(def trace   (method :trace))
