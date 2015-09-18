(ns example.services.data)

(defmulti handler (fn [event args] event))

(defmethod handler
  :login
  [_ args]
  [:success {:token "foobar"}])
