(ns ^:figwheel-always test-app.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]))

(defmulti handler
  (fn [event args cursor] event))

(defmethod handler
  :login
  [_ {:keys [email password]} cursor]
  (println "Logging in..." email password))

(defmethod handler
  :test-event
  [_ new-text cursor]
  (om/update! cursor :text new-text))
