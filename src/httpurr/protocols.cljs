(ns httpurr.protocols
  "The protocols in which the HTTP client is based.")

(defprotocol Client
  (send! [_ request options]
   "Given a request and options, perform the request and return a value
   that implements the `Request` protocol."))

(defprotocol Request
  (listen! [_ cb]
    "Call the given `cb` function with a type that implements `Response`
    when the request completes"))

(defprotocol Abort
  (abort! [_]
    "Abort a request."))

(defprotocol Response
  (success? [_]
    "Return `true` if a response was returned from the server.")
  (response [_]
    "Given a response that has completed successfully, return the response
     map.")
  (error [_]
    "Given a request that has completed with an error, return the keyword
     corresponding to its error."))
