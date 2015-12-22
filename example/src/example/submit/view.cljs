(ns ^:figwheel-always example.submit.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defcomponent view
  [{:keys [foo bar text]} owner & opts]
  (render [_]
          (html
           [:div
            [:h2 text]
            [:h3 (str "foo=" foo)]
            [:h3 (str "bar=" bar)]
            [:h4
             [:a {:href (venue/get-route :views/submit {:foo foo :bar (inc (js/parseInt bar))} {:no-history true})} "inc"]]
            [:h4
             [:a {:href (venue/get-route :views/submit {:query-params {:bananas "12"} :foo foo :bar (dec (js/parseInt bar))} {:no-history true})} "dec"]]])))
