(ns httpurr.errors
  "A namespace containing the errors that happen when no response is returned
  and predicates for checking the error type.")

(def timeout :timeout)
(def timeout?
  (partial = timeout))

(def exception :exception)
(def exception?
  (partial = exception))

(def http-error :http-error)
(def http-error?
  (partial = http-error))

(def abort :abort)
(def abort?
  (partial = abort))
