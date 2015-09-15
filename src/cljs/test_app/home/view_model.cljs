(ns ^:figwheel-always test-app.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]))

(defmulti handler
  (fn [event args] event))

(defmethod handler
  :test-event
  [_ args]
  (println "Got test event"))
