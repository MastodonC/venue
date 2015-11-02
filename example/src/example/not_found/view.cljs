(ns ^:figwheel-always example.not-found.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defcomponent view
  [cursor owner & opts]
  (render [_]
          (html
           [:div
            [:h3 "404 - Not Found"]])))
