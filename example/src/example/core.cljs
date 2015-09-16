(ns example.core
  (:require [venue.core :as venue :include-macros true]
            ;;
            [example.home.view]
            [example.home.view-model]
            [example.submit.view]
            [example.submit.view-model]
            [example.menu.view]
            [example.menu.view-model])
  (:require-macros [cljs-log.core :as log]))

(venue/defview!
  {:target "app"
   :route "/"
   :id :views/home
   :view example.home.view/view
   :view-model example.home.view-model/handler
   :state {:text "Home Page"}})

(venue/defview!
  {:target "app"
   :route "/submit"
   :id :views/submit
   :view example.submit.view/view
   :view-model example.submit.view-model/handler
   :state {:text "Submit Page"}})

(venue/defstatic!
  {:target "menu"
   :id :static/menu
   :view example.menu.view/view
   :view-model example.menu.view-model/handler
   :state {}})

(comment (venue/start!))
