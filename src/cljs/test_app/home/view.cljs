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
            [:div
             [:h1 (:text cursor)]
             [:h2 "Welome to my blog"]
             [:button {:on-click #(venue/raise! owner :test-event "This was changed by the view-model")} "Change title"]]
            [:div
             [:input {:ref "email" :type "email" :placeholder "Email"}]
             [:input {:ref "password" :type "password" :placeholder "Password"}]
             [:button {:on-click #(do
                                    (venue/raise! owner :login
                                                  {:email (.-value (om/get-node owner "email"))
                                                   :password (.-value (om/get-node owner "password"))})
                                    (.preventDefault %))} "Login"]]])))
