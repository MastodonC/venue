(ns ^:figwheel-always example.menu.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defmulti event-handler
  (fn [event args cursor] event))

(defmulti response-handler
  (fn [result response cursor] result))

(defn view-model
  []
  (reify
    venue/IHandleEvent
    (handle-event [owner event args cursor]
      (event-handler event args cursor))
    venue/IHandleResponse
    (handle-response [owner outcome event response cursor]
      (response-handler [event outcome] response cursor))
    venue/IActivate
    (activate [owner args cursor])))

;;;;;;;;;;;;

(defmethod event-handler
  :default
  [_ _ _]
  (log/debug "Hello from the menu"))
