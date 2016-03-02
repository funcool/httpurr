(ns httpurr.status
  "A namespace of constants for HTTP status codes and predicates for discerning
  the types of responses.")

;; 1xx informational

(defn informational?
  [{:keys [status]}]
  (<= 100 status 199))

(def continue 100)
(def switching-protocols 101)
(def processing 102)

(def informational-codes #{continue
                           switching-protocols
                           processing})

;; 2xx success
(defn success?
  [{:keys [status]}]
  (<= 200 status 299))

(def ok 200)
(def created 201)
(def accepted 202)
(def non-authoritative-information 203)
(def no-content 204)
(def reset-content 205)
(def partial-content 206)
(def multi-status 207)
(def already-reported 208)
(def im-used 226)

(def success-codes #{ok
                     created
                     accepted
                     non-authoritative-information
                     no-content
                     reset-content
                     partial-content
                     multi-status
                     already-reported
                     im-used})

;; 3xx redirection
(defn redirection?
  [{:keys [status]}]
  (<= 300 status 399))

(def multiple-choices 300)
(def moved-permanently 301)
(def found 302)
(def see-other 303)
(def not-modified 304)
(def use-proxy 305)
(def temporary-redirect 307)
(def permanent-redirect 308)

(def redirection-codes #{multiple-choices
                         moved-permanently
                         found
                         see-other
                         not-modified
                         use-proxy
                         temporary-redirect
                         permanent-redirect})

;; 4xx client error
(defn client-error?
  [{:keys [status]}]
  (<= 400 status 499))

(def bad-request 400)
(def unauthorized 401)
(def payment-required 402)
(def forbidden 403)
(def not-found 404)
(def method-not-allowed 405)
(def not-acceptable 406)
(def proxy-authentication-required 407)
(def request-timeout 408)
(def conflict 409)
(def gone 410)
(def length-required 411)
(def precondition-failed 412)
(def payload-too-large 413)
(def request-uri-too-long 414)
(def unsupported-media-type 415)
(def request-range-not-satisfieable 416)
(def expectation-failed 417)
(def authentication-timeout 419)
(def precondition-required 428)
(def too-many-requests 429)
(def request-header-fields-too-large 431)

(def client-error-codes #{bad-request
                          unauthorized
                          payment-required
                          forbidden
                          not-found
                          method-not-allowed
                          not-acceptable
                          proxy-authentication-required
                          request-timeout
                          conflict
                          gone
                          length-required
                          precondition-failed
                          payload-too-large
                          request-uri-too-long
                          unsupported-media-type
                          request-range-not-satisfieable
                          expectation-failed
                          authentication-timeout
                          precondition-required
                          too-many-requests
                          request-header-fields-too-large})

;; 5xx server error
(defn server-error?
  [{:keys [status]}]
  (<= 500 status 599))

(def internal-server-error 500)
(def not-implemented 501)
(def bad-gateway 502)
(def service-unavailable 503)
(def gateway-timeout 504)
(def http-version-not-supported 505)
(def network-authentication-required 511)

(def server-error-codes #{internal-server-error
                          not-implemented
                          bad-gateway
                          service-unavailable
                          gateway-timeout
                          http-version-not-supported
                          network-authentication-required})

;; 4-5xx
(defn error?
  [resp]
  (or (client-error? resp)
      (server-error? resp)))

;; xxx
(defn status-code?
  [status]
  (<= 100 status 599))
