(ns ^:figwheel-always example.menu.view-model
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]))

(defn handler
  [event args]
  (println "Got event" event))
