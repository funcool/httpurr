(ns httpurr.status
  "A namespace of constants for HTTP status codes and predicates for discerning
  the types of responses."
 (:import [goog.net HttpStatus]))

;; 1xx informational

(defn informational?
  [{:keys [status]}]
  (<= 100 status 199))

(def continue
  (.-CONTINUE HttpStatus))

(def switching-protocols
  (.-SWITCHING_PROTOCOLS HttpStatus))

(def processing 102)

(def informational-codes #{continue
                           switching-protocols
                           processing})

;; 2xx success
(defn success?
  [{:keys [status]}]
  (<= 200 status 299))

(def ok
  (.-OK HttpStatus))

(def created
  (.-CREATED HttpStatus))

(def accepted
  (.-ACCEPTED HttpStatus))

(def non-authoritative-information 203)

(def no-content
  (.-NO_CONTENT HttpStatus))

(def reset-content
  (.-RESET_CONTENT HttpStatus))

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

(def multiple-choices
  (.-MULTIPLE_CHOICES HttpStatus))

(def moved-permanently
  (.-MOVED_PERMANENTLY HttpStatus))

(def found
  (.-FOUND HttpStatus))

(def see-other
  (.-SEE_OTHER HttpStatus))

(def not-modified
  (.-NOT_MODIFIED HttpStatus))

(def use-proxy
  (.-USE_PROXY HttpStatus))

(def temporary-redirect
  (.-TEMPORARY_REDIRECT HttpStatus))

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

(def bad-request
  (.-BAD_REQUEST HttpStatus))

(def unauthorized
  (.-UNAUTHORIZED HttpStatus))

(def payment-required
  (.-PAYMENT_REQUIRED HttpStatus))

(def forbidden
  (.-FORBIDDEN HttpStatus))

(def not-found
  (.-NOT_FOUND HttpStatus))

(def method-not-allowed
  (.-METHOD_NOT_ALLOWED HttpStatus))

(def not-acceptable
  (.-NOT_ACCEPTABLE HttpStatus))

(def proxy-authentication-required
  (.-PROXY_AUTHENTICATION_REQUIRED HttpStatus))

(def request-timeout
  (.-REQUEST_TIMEOUT HttpStatus))

(def conflict
  (.-CONFLICT HttpStatus))

(def gone
  (.-GONE HttpStatus))

(def length-required
  (.-LENGTH_REQUIRED HttpStatus))

(def precondition-failed
  (.-PRECONDITION_FAILED HttpStatus))

(def payload-too-large
  (.-REQUEST_ENTITY_TOO_LARGE HttpStatus))

(def request-uri-too-long
  (.-REQUEST_URI_TOO_LONG HttpStatus))

(def unsupported-media-type
  (.-UNSUPPORTED_MEDIA_TYPE HttpStatus))

(def request-range-not-satisfieable
  (.-REQUEST_RANGE_NOT_SATISFIABLE HttpStatus))

(def expectation-failed
  (.-EXPECTATION_FAILED HttpStatus))

(def authentication-timeout 419)

(def precondition-required
  (.-PRECONDITION_REQUIRED HttpStatus))

(def too-many-requests
  (.-TOO_MANY_REQUESTS HttpStatus))

(def request-header-fields-too-large
  (.-REQUEST_HEADER_FIELDS_TOO_LARGE HttpStatus))

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

(def internal-server-error
  (.-INTERNAL_SERVER_ERROR HttpStatus))

(def not-implemented
  (.-NOT_IMPLEMENTED HttpStatus))

(def bad-gateway
  (.-BAD_GATEWAY HttpStatus))

(def service-unavailable
  (.-SERVICE_UNAVAILABLE HttpStatus))

(def gateway-timeout
  (.-GATEWAY_TIMEOUT HttpStatus))

(def http-version-not-supported
  (.-HTTP_VERSION_NOT_SUPPORTED HttpStatus))

(def network-authentication-required
  (.-NETWORK_AUTHENTICATION_REQUIRED HttpStatus))

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
