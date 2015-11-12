(ns httpurr.auth
  (:import [goog.crypt base64]))

(defn www-auth-header
  [realm]
  (str "Basic realm=\"" realm "\""))

(defn auth-header
  [user password]
  (str "Basic " (base64/encodeString (str user ":" password))))

(defn basic
  [realm user password]
  (fn [req]
    (update req
            :headers
            (partial merge {"WWW-Authenticate" (www-auth-header realm)
                            "Authorization" (auth-header user password)}))))
