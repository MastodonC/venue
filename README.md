# Venue

Venue is an MVVM-inspired ClojureScript framework for creating [single-page applications](https://en.wikipedia.org/wiki/Single-page_application). It uses Om, Secretary, DataScript and a handful of other CLJS libraries. It's opinionated and expects applications to adhere to the principles of [MVVM] (https://en.wikipedia.org/wiki/Model_View_ViewModel): databinding is provided by Om/React, models are ~~facilitated by DataScript~~ coming soon.

[![Clojars Project](http://clojars.org/venue/latest-version.svg)](http://clojars.org/venue)

#### Notes when using Figwheel

In order to use Figwheels auto-reload during development, you should call `(venue/on-js-reload)` inside your own `on-js-reload` function.

## Example
```clojure
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

(venue/defview!
  {:target "app"
   :route "/"
   :id :views/home
   :view test-app.home.view/view
   :view-model test-app.home.view-model/view-model
   :state {:text "Home Page"}})

(venue/defview!
  {:target "app"
   :route "/submit"
   :id :views/submit
   :view test-app.submit.view/view
   :view-model test-app.submit.view-model/view-model
   :state {:text "Submit Page"}})

(venue/defstatic!
  {:target "menu"
   :id :static/menu
   :view test-app.menu.view/view
   :view-model test-app.menu.view-model/view-model
   :state {}})

(defn on-js-reload [] (venue/on-js-reload)) ;; for figwheel, hooked up in project.clj

(venue/start!) ;; <-- don't forget this bit!
```
This contrived example defines two views destined for the same target, based on different routes. It also defines a static view (always present, not subject to routing). Behind the curtain, venue will activate the appropriate view based on the current route. It will also send any events raised by that view to the applicable view-model for handling.

Views are just Om components (the example uses om-tools):

```clojure
;; test-app.home.view
(defcomponent view
  [cursor owner & opts]
  (render [_]
          (html
           [:div
             [:h1 (:text cursor)] ;; will display "Home Page"
             [:a {:href (venue/get-route :views/submit {:query-params {:action "new")} "Submit New"] ;; will generate route from view id, i.e. '#/submit?action=new'
             [:button {:on-click #(venue/raise! owner :test-event "Clicked!")} "Change text"]])))

```

View-Models are reify-ed functions, very similar to the way in which Om components are built:

```clojure
;; test-app.home.view-model
(defmulti handler
  (fn [event args cursor ctx] event))

(defmethod handler
  :test-event
  [_ new-text cursor _]
  (om/update! cursor :text new-text))

(defn view-model
  [ctx]
  (reify
    venue/IHandleEvent
    (handle-event [_ event args cursor]
      (handler event args cursor ctx))
    venue/IActivate
    (activate [_ args cursor]))))
```

## Rationale

![venue application design](http://i.imgur.com/ce8RLIu.png)

Currently, Venue manages views and view-models but we'll be expanding it to provide service functionality.

The world of ClojureScript applications is still in the Triassic period. There is a selection of "preferred" utility libraries that seem to reoccur and be evolving quicker than the rest, and a few people have actually put out [massively successful applications which rely on them](https://github.com/circleci/frontend). However, there's still a huge learning curve to understand all of these technologies, how they fit together and how to build a sensible application using them. At MastodonC we ~~have been through this~~ are going through this and so Venue is our attempt at providing a framework which helps.

#### Why MVVM?

Why not? Just because this is 'new technology' doesn't mean the tales of old don't apply. MVVM helps (as do the whole MV*-family) answer a lot of questions regarding application architecture and as Om/React gives us a very easy way of data-binding (one of the core features of MVVM that is usually tough to do without a mature UI framework) it makes sense that we capitalise on that.

## Tests

This library has a bunch of tests with it which you can run using.

```clojure
lein doo slimer test once
```
You will need Nodejs (>= 0.12, <= 0.12.7) and slimerjs (`npm install -g slimerjs`) in order to do this.

## License

Copyright © 2014 MastodonC

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

## Using Venue?

Please let us know! support@mastodonc.com
