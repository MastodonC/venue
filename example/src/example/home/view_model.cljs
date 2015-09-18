(ns ^:figwheel-always example.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defmulti event-handler
  (fn [event this args cursor] event))

(defmulti response-handler
  (fn [result data] result))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :login
  [_ this {:keys [email password] :as login-args} cursor]
  (log/debug "Logging in..." email password)
  (venue/request! this :service/data :login login-args))

(defmethod event-handler
  :test-event
  [_ this new-text cursor]
  (om/update! cursor :text new-text))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:login :success]
  [_ data]
  (log/debug "RESPONSE HANDLER SUCCESS"))

(defmethod response-handler
  [:login :failure]
  [_ data]
  (log/debug "RESPONSE HANDLER FAILURE"))

(defn view-model
  []
  (reify
    venue/IHandleEvent
    (handle-event [this event args cursor]
      (event-handler event this args cursor))
    venue/IHandleResponse
    (handle-response [this outcome event data]
      (response-handler [event outcome] data))
    venue/IActivate
    (activate [_ args cursor])))
