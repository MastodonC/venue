(ns ^:figwheel-always example.home.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [venue.core :as venue :include-macros true])
    (:require-macros [cljs-log.core :as log]))


(declare on-initialise)
(declare on-activate)

(defmulti event-handler
           (fn [owner event args cursor] event))

(defmulti response-handler
           (fn [owner result response context] result))

(defn view-model
  []
  (reify
    venue/IHandleEvent
    (handle-event [owner event args cursor]
                    (event-handler owner event args cursor))
    venue/IHandleResponse
    (handle-response [owner outcome event response context]
                       (response-handler owner [event outcome] response context))
    venue/IActivate
    (activate [owner args cursor]
      (on-activate owner args cursor))

    venue/IDeactivate
    (deactivate [owner cursor]
      (log/info "Deactivating home"))

    venue/IInitialise
    (initialise [owner cursor]
      (on-initialise owner cursor))))

(defn on-initialise [_ _])
(defn on-activate [_ _ _])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :login
  [owner {:keys [email password] :as login-args} cursor]
  (log/debug "Logging in..." email password)
  (venue/request! {:owner owner
                   :service :service/data
                   :request :login
                   :args login-args
                   :context cursor
                   :timeout? false}))

(defmethod event-handler
  :test-event
  [_ new-text cursor]
  (om/update! cursor :text new-text))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:login :success]
  [_ response cursor]
  (log/debug "RESPONSE HANDLER SUCCESS"))

(defmethod response-handler
  [:login :failure]
  [_ response cursor]
  (log/debug "RESPONSE HANDLER FAILURE:" response))
