(ns test-app.core
  (:require [venue.core :as venue :include-macros true]
            ;;
            [test-app.home.view]
            [test-app.home.view-model]
            [test-app.submit.view]
            [test-app.submit.view-model]
            [test-app.menu.view]
            [test-app.menu.view-model])
  (:require-macros [cljs-log.core :as log]))

(defonce init
  (do
    (venue/defview!
      {:target "app"
       :route "/"
       :id :views/home
       :view test-app.home.view/view
       :view-model test-app.home.view-model/handler
       :state {:text "Hola hola"}})

    (comment (venue/define-fixtures!
               {:target "app"}
               [{:route "/"
                 :id :views/home
                 :view test-app.home.view/view
                 :view-model test-app.home.view-model/handler
                 :state {:text "Hola hola"}}

                ;; {:route "/submit"
                ;;  :id :views/submit
                ;;  :view test-app.submit.view/view
                ;;  :view-model test-app.submit.view-model/handler
                ;;  :state {:text "I am sparta"}}
                ]))

    (comment (venue/define-static!
               {:target "menu"
                :id :menu
                :view test-app.menu.view/view
                :view-model test-app.menu.view-model/handler
                :state {}}))

    (venue/start!)))
