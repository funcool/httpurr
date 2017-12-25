(ns httpurr.client.xhr-alt
  (:refer-clojure :exclude [get])
  (:require [httpurr.protocols]
            [clojure.string :as str]
            [httpurr.protocols :as p]
            [httpurr.client :as c]))

(defn normalize-headers
  [headers]
  (reduce-kv (fn [acc k v]
               (assoc acc (str/lower-case k) v))
             {} headers))

(defn- parse-header-string [hs]
  (when-not (empty? hs)
    (->> hs
         clojure.string/split-lines
         (map #(clojure.string/split % ": "))
         (into {}))))

(deftype Xhr [xhr]
  p/Request
  (-listen [_ cb]
    (set! (.-onreadystatechange xhr)
          (fn []
            (when (= (.-readyState xhr)
                     js/XMLHttpRequest.DONE)
              (cb (Xhr. xhr))))))

  p/Response
  (-success? [_]
    (#{200 201 202 204 206 304 1223} (.-status xhr)))

  (-response [_]
    {:status  (.-status xhr)
     :body    (.-response xhr)
     :headers (-> (.getAllResponseHeaders xhr)
                  parse-header-string
                  (normalize-headers))})

  (-error [this]
    (ex-info "Error" {:status      (.-status xhr)
                      :status-text (.-statusText xhr)
                      :body        (.-response xhr)
                      :headers     (-> (.getAllResponseHeaders xhr)
                                       parse-header-string
                                       (normalize-headers))})))

(defn make-uri
  [url qs qp]
  (let [qs' (->> (clojure.string/split qs #"&")
                 (map #(clojure.string/split % #"=")))
        qp' (map (fn [[k v]]
                   [(name k) v])
                 qp)
        query-string (->> (concat qp' qs')
                          (apply concat)
                          (map js/encodeURIComponent)
                          (partition 2)
                          (map (partial clojure.string/join "="))
                          (clojure.string/join "&"))]
    (str url (when-not (empty? query-string)
               (str "?" query-string)))))

(def client
  (reify p/Client
    (-send [_ request options]
      (let [{:keys [timeout with-credentials?] :or {timeout 0 with-credentials? false}} options
            {:keys [method url query-string query-params headers body]} request
            uri (make-uri url query-string query-params)
            method (c/keyword->method method)
            xhr (js/XMLHttpRequest.)]
        (.open xhr method uri)
        (set! (.-timeout xhr) timeout)
        (set! (.-withCredentials xhr) with-credentials?)
        (doseq [[k v] headers]
          (.setRequestHeader xhr k v))
        (.send xhr body)
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