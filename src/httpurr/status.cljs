(ns httpurr.status
  "A namespace of constants for HTTP status codes and predicates for discerning
  the types of responses."
 (:import [goog.net HttpStatus]))

;; 1xx informational

(defn informational?
  [{:keys [status]}]
  (<= 100 status 199))

(def continue HttpStatus.CONTINUE)
(def switching-protocols HttpStatus.SWITCHING_PROTOCOLS)
(def processing 102)

(def informational-codes #{continue
                           switching-protocols
                           processing})

;; 2xx success
(defn success?
  [{:keys [status]}]
  (<= 200 status 299))

(def ok HttpStatus.OK)
(def created HttpStatus.CREATED)
(def accepted HttpStatus.ACCEPTED)
(def non-authoritative-information 203)
(def no-content HttpStatus.NO_CONTENT)
(def reset-content HttpStatus.RESET_CONTENT)
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

(def multiple-choices HttpStatus.MULTIPLE_CHOICES)
(def moved-permanently HttpStatus.MOVED_PERMANENTLY)
(def found HttpStatus.FOUND)
(def see-other HttpStatus.SEE_OTHER)
(def not-modified HttpStatus.NOT_MODIFIED)
(def use-proxy HttpStatus.USE_PROXY)
(def temporary-redirect HttpStatus.TEMPORARY_REDIRECT)
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

(def bad-request HttpStatus.BAD_REQUEST)
(def unauthorized HttpStatus.UNAUTHORIZED)
(def payment-required HttpStatus.PAYMENT_REQUIRED)
(def forbidden HttpStatus.FORBIDDEN)
(def not-found HttpStatus.NOT_FOUND)
(def method-not-allowed HttpStatus.METHOD_NOT_ALLOWED)
(def not-acceptable HttpStatus.NOT_ACCEPTABLE)
(def proxy-authentication-required HttpStatus.PROXY_AUTHENTICATION_REQUIRED)
(def request-timeout HttpStatus.REQUEST_TIMEOUT)
(def conflict HttpStatus.CONFLICT)

(def gone HttpStatus.GONE)
(def length-required HttpStatus.LENGTH_REQUIRED)
(def precondition-failed HttpStatus.PRECONDITION_FAILED)
(def payload-too-large HttpStatus.REQUEST_ENTITY_TOO_LARGE)
(def request-uri-too-long HttpStatus.REQUEST_URI_TOO_LONG)
(def unsupported-media-type HttpStatus.UNSUPPORTED_MEDIA_TYPE)
(def request-range-not-satisfieable HttpStatus.REQUEST_RANGE_NOT_SATISFIABLE)

(def expectation-failed HttpStatus.EXPECTATION_FAILED)
(def authentication-timeout 419)
(def precondition-required HttpStatus.PRECONDITION_REQUIRED)
(def too-many-requests HttpStatus.TOO_MANY_REQUESTS)
(def request-header-fields-too-large HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)

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

(def internal-server-error HttpStatus.INTERNAL_SERVER_ERROR)
(def not-implemented HttpStatus.NOT_IMPLEMENTED)
(def bad-gateway HttpStatus.BAD_GATEWAY)
(def service-unavailable HttpStatus.SERVICE_UNAVAILABLE)
(def gateway-timeout HttpStatus.GATEWAY_TIMEOUT)
(def http-version-not-supported HttpStatus.HTTP_VERSION_NOT_SUPPORTED)
(def network-authentication-required HttpStatus.NETWORK_AUTHENTICATION_REQUIRED)

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
