(ns ^:figwheel-always test-app.menu.view
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
            [:h3 {:style {:display "inline"}} "BLOG MENU | "]
            [:a {:href (venue/get-route :views/home)} "Read Blog"]
            [:span " | "]
            [:a {:href (venue/get-route :views/submit)} "Submit Post"]
            ])))
