(ns httpurr.client.util)

(defn prepare-headers
  [headers]
  (let [h (or headers {})]
    (if (empty? h)
      #js {}
      (clj->js h))))
