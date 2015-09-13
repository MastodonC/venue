(ns test-app.views.hello
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [venue.core :as mvvm])
  (:require-macros [cljs-log.core :as log]))

(defcomponent view
  [cursor owner & opts]
  (render [_]
          (html
           [:div
            [:h1 (:text cursor)]
            [:span (str (om/cursor? cursor))]
            [:button {:on-click #(om/update! cursor [:text] "hahah")} "Press"]
            [:button {:on-click #(mvvm/navigate! :views/goodbye)} "Navigate"]])))
