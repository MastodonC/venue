(ns ^:figwheel-always example.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defmulti handler
  (fn [event args cursor ctx] event))

(defmethod handler
  :login
  [_ {:keys [email password]} cursor ctx]
  (log/debug "Logging in..." email password))

(defmethod handler
  :test-event
  [_ new-text cursor _]
  (om/update! cursor :text new-text))

(defn view-model
  [ctx]
  (reify
    venue/IHandleEvent
    (handle-event [_ event args cursor]
      (handler event args cursor ctx))
    venue/IActivate
    (activate [_ args cursor])))
