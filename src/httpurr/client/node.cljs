(ns httpurr.client.node
  (:refer-clojure :exclude [get])
  (:require [cljs.nodejs :as node]
            [httpurr.client :as c]
            [httpurr.client.util :refer [prepare-headers]]
            [httpurr.protocols :as p]))

(def ^:private http (node/require "http"))
(def ^:private https (node/require "https"))
(def ^:private url (node/require "url"))

(defn url->options
  [u]
  (let [parsed (.parse url u)]
    {:protocol (.-protocol parsed)
     :host (.-hostname parsed)
     :port (.-port parsed)
     :path (.-pathname parsed)
     :query (.-query parsed)}))

(deftype HttpResponseError [err]
  p/Response
  (-success? [_] false)
  (-error [_] err))

(deftype HttpResponse [msg]
  p/Response
  (-success? [_] true)
  (-response [_]
    (let [headersv (partition 2 (js->clj (.-rawHeaders msg)))]
      {:status  (.-statusCode msg)
       :body    (.read msg)
       :headers (zipmap
                 (map first headersv)
                 (map second headersv))})))

(deftype HttpRequest [req]
  p/Request
  (-listen [_ callback]
    (letfn [(listen [target event cb]
              (.on target event cb))
            (on-abort [err]
              ;; (js/console.log "on-abort")
              (callback (HttpResponseError. :abort)))
            (on-response [msg]
              ;; (js/console.log "on-response")
              (listen msg "readable" (partial on-message msg)))
            (on-message [msg]
              ;; (js/console.log "on-message")
              (callback (HttpResponse. msg)))
            (on-timeout [err]
              ;; (js/console.log "on-timeout" err)
              (callback (HttpResponseError. :timeout)))
            (on-client-error [err]
              ;; (js/console.log "on-client-error")
              (callback (HttpResponseError. :http)))
            (on-error [err]
              ;; (js/console.log "on-error" err)
              (callback (HttpResponseError. :exception)))]
      (listen req "response" on-response)
      (listen req "abort" on-abort)
      (listen req "timeout" on-timeout)
      (listen req "clientError" on-client-error)
      (listen req "error" on-error)))

  p/Abort
  (-abort [_]
    (.abort req)))

(def client
  (reify p/Client
    (-send [_ request {timeout :timeout :or {timeout 0} :as options}]
      (let [{:keys [method url headers body]} request
            urldata (url->options url)
            options (merge (dissoc urldata :query)
                           {:headers (prepare-headers headers)
                            :method (c/keyword->method method)}
                           (when (:query urldata)
                             {:path (str (:path urldata) "?" (:query urldata))})
                           (when (:query-string request)
                             {:path (str (:path urldata) "?" (:query-string request))}))
            https? (= "https:" (:protocol options))
            req (.request (if https? https http) (clj->js options))]
        (.setTimeout req timeout)
        (when body (.write req body))
        (.end req)
        (HttpRequest. req)))))

(def send! (partial c/send! client))
(def head (partial (c/method :head) client))
(def options (partial (c/method :options) client))
(def get (partial (c/method :get) client))
(def post (partial (c/method :post) client))
(def put (partial (c/method :put) client))
(def patch (partial (c/method :patch) client))
(def delete (partial (c/method :delete) client))
(def trace (partial (c/method :trace) client))
