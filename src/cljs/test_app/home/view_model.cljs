(ns ^:figwheel-always test-app.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]))

(defmulti handler
  (fn [event args] event))

(defmethod handler
  :login
  [_ {:keys [email password]}]
  (println "Logging in..." email password))
