(ns httpurr.content.json)

(defn decode
  [s]
  (js/JSON.parse s))

(defn encode
  [obj]
  (js/JSON.stringify obj))
