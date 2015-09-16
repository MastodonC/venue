# Venue

Venue is an MVVM-inspired ClojureScript framework for creating [single-page applications](https://en.wikipedia.org/wiki/Single-page_application). It uses Om, Secretary, DataScript and a handful of other CLJS libraries. It's opinionated and expects applications to adhere to the principles of [MVVM] (https://en.wikipedia.org/wiki/Model_View_ViewModel): databinding is provided by Om/React, models are ~~facilitated by DataScript~~ coming soon.

[![Clojars Project](http://clojars.org/venue/latest-version.svg)](http://clojars.org/venue)

#### Notes when using Figwheel

In order to use Figwheels auto-reload during development, you should call `(venue/on-js-reload)` inside your own `on-js-reload` function.

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
   :view-model test-app.home.view-model/handler
   :state {:text "Home Page"}})

(venue/defview!
  {:target "app"
   :route "/submit"
   :id :views/submit
   :view test-app.submit.view/view
   :view-model test-app.submit.view-model/handler
   :state {:text "Submit Page"}})

(venue/defstatic!
  {:target "menu"
   :id :static/menu
   :view test-app.menu.view/view
   :view-model test-app.menu.view-model/handler
   :state {}})

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
           [:button {:on-click #(venue/raise! owner :test-event "Clicked!")} "Change text"]])))

```

View-Models are just functions (the example uses a multi-method):

```clojure
;; test-app.home.handler
(defmulti handler
  (fn [event args cursor] event))

(defmethod handler
  :test-event
  [_ new-text cursor]
  (om/update! cursor :text new-text))
```

## Rationale

![venue application design](http://i.imgur.com/ce8RLIu.png)

The world of ClojureScript applications is still in the Triassic period. There is a selection of "preferred" utility libraries that seem to reoccur and be evolving quicker than the rest, and a few people have actually put out [massively successful applications which rely on them](https://github.com/circleci/frontend). However, there's still a huge learning curve to understand all of these technologies, how they fit together and how to build a sensible application using them. At MastodonC we ~~have been through this~~ are going through this and so Venue is our attempt at providing a framework which helps.

#### Why MVVM?

Why not? Just because this is 'new technology' doesn't mean the tales of old don't apply. MVVM helps (as do the whole MV*-family) answer a lot of questions regarding application architecture and as Om/React gives us a very easy way of data-binding (one of the core features of MVVM that is usually tough to do without a mature UI framework) it makes sense that we capitalise on that.

## License

Copyright © 2014 MastodonC

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

## Using Venue?

Please let us know! support@mastodonc.com
