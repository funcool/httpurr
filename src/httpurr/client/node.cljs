(ns httpurr.client.node
  (:refer-clojure :exclude [get])
  (:require [cljs.nodejs :as node]
            [clojure.string :as s]
            [httpurr.client :as c]
            [httpurr.protocols :as p]))

(def ^:private http (node/require "http"))
(def ^:private https (node/require "https"))
(def ^:private url (node/require "url"))
(def ^:private querystring (node/require "querystring"))

(defn- url->options
  [u qs qp]
  (let [parsed (.parse url u)]
    (merge
     {:protocol (.-protocol parsed)
      :host (.-hostname parsed)
      :port (.-port parsed)
      :path (.-pathname parsed)
      :query (.-query parsed)}
     (when qs {:query qs})
     (when qp {:query (.stringify querystring (clj->js qp))}))))

(deftype HttpResponse [msg body]
  p/Response
  (-success? [_] true)
  (-response [_]
    (let [headersv (partition 2 (js->clj (.-rawHeaders msg)))]
      {:status  (.-statusCode msg)
       :body    body
       :headers (zipmap
                 (map first headersv)
                 (map second headersv))})))

(deftype HttpResponseError [type err]
  p/Response
  (-success? [_] false)
  (-error [_]
    (if err
      (ex-info (.-message err) {:type type :code (.-code err)})
      (ex-info "" {:type type}))))

(deftype HttpRequest [req]
  p/Request
  (-listen [_ callback]
    (letfn [(listen [target event cb]
              (.on target event cb))
            (on-abort [err]
              (callback (HttpResponseError. :abort nil)))
            (on-response [msg]
              (let [chunks (atom [])]
                (listen msg "readable" #(swap! chunks conj (.read msg)))
                (listen msg "end" #(callback (HttpResponse. msg (s/join "" @chunks))))))
            (on-timeout [err]
              (callback (HttpResponseError. :timeout nil)))
            (on-client-error [err]
              (callback (HttpResponseError. :client-error err)))
            (on-error [err]
              (callback (HttpResponseError. :exception err)))]
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
      (let [{:keys [method query-string query-params url headers body]} request
            urldata (url->options url query-string query-params)
            options (merge (dissoc urldata :query)
                           {:headers (if headers (clj->js headers) #js {})
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
