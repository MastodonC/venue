(ns ^:figwheel-always example.not-found.view-model
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
    venue/IInitialise
    (initialise [owner cursor]
      (on-initialise owner cursor))))

(defn on-initialise [_ _])
(defn on-activate [_ _ _])
