(ns ^:figwheel-always example.menu.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defn handler
  [event args cursor ctx]
  (println "Got event" event))

(defn view-model
  [ctx]
  (reify
    venue/IHandleEvent
    (handle-event [_ event args cursor]
      (handler event args cursor ctx))
    venue/IWillMount
    (will-mount [_ args cursor]
      (log/info "WILL MOUNT CALLED"))))
