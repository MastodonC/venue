(ns ^:figwheel-always test-app.home.view
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
            [:h1 (:text cursor)]
            [:h2 "Some intro"]
            [:button {:on-click #(om/update! cursor [:text] "Don't fiddle with state!")} "Change state"]
            [:button {:on-click #(venue/navigate! :views/submit)} "Navigate"]
            [:button {:on-click #(do
                                   (venue/raise! owner :test-event {:foo "bar"})
                                   (.preventDefault %))} "Event"]])))
