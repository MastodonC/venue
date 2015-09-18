(ns ^:figwheel-always example.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [venue.core :as venue :include-macros true])
    (:require-macros [cljs-log.core :as log]))

(defmulti event-handler
  (fn [event owner args cursor] event))

(defmulti response-handler
    (fn [result owner data] result))

(defn view-model
  []
  (reify
    venue/IHandleEvent
    (handle-event [owner event args cursor]
      (event-handler event owner args cursor))
    venue/IHandleResponse
    (handle-response [owner outcome event data]
      (response-handler [event outcome] owner data))
    venue/IActivate
    (activate [owner args cursor])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :login
  [_ owner {:keys [email password] :as login-args} cursor]
  (log/debug "Logging in..." email password)
  (venue/request! owner :service/data :login login-args))

(defmethod event-handler
  :test-event
  [_ owner new-text cursor]
  (om/update! cursor :text new-text))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:login :success]
  [_ owner data]
  (log/debug "RESPONSE HANDLER SUCCESS"))

(defmethod response-handler
  [:login :failure]
  [_ owner data]
  (log/debug "RESPONSE HANDLER FAILURE"))
