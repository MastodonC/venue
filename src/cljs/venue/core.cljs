(ns ^:figwheel-always venue.core
    (:require [cljs.core.async :as async :refer [chan]]
              [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [schema.core :as s :include-macros true]
              [secretary.core :as secretary :refer-macros [defroute]])
    (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                     [cljs-log.core :as log])
    (:import goog.History))


(defonce app-state (atom {}))
(defonce mvvm-state (atom {}))
(def wildcard-route "*")
(def history (History.))
(def log-prefix "[venue]")

(defn log-debug  [& body] (log/debug  log-prefix " " (apply str body)))
(defn log-info   [& body] (log/info   log-prefix " " (apply str body)))
(defn log-warn   [& body] (log/warn   log-prefix " " (apply str body)))
(defn log-severe [& body] (log/severe log-prefix " " (apply str body)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;

(secretary/set-config! :prefix "#")

(defn route-by-id
  [id]
  (->> @mvvm-state
       (map val)
       (mapcat :routes)
       (map (partial apply hash-map))
       (reduce conj)
       id))

(defn route-list
  []
  (->> @mvvm-state
       (map val)
       (mapcat :routes)
       (map second)
       (map :route)
       set))

(defn filter-routes
  [location]
  (->> @mvvm-state
       (map val)
       (mapcat :routes)
       (map second)
       (filter #(or (= (:route %) wildcard-route) (= (:route %) location)))))

(defn navigate!
  [route-id]

  ;; TODO - check we're not nav-ing to a wildcard route. Disallowed!

  (let [route (->> route-id route-by-id :route)]
    (log-info "Navigating to " route)
    (set! (.. js/document -location -href) (str "#" route))))

(defn start!
  []
  (log-info "Starting up...")
  (secretary/dispatch! js/window.location.hash))

(defn- launch-route!
  [location]
  (let [mvvm-cursor (om/root-cursor mvvm-state)]
    ;; loop routes applicable to this location
    (doseq [{:keys [target id route]} (filter-routes location)]
      (log-debug "Activating " route " into " target)
      (if-let [target-element (. js/document (getElementById (name target)))]
        (do
          ;; if we're not installed, add an om/root
          (when (not (:installed? (get mvvm-cursor target)))
            (log-debug target " does not have an om/root. Installing now...")
            (om/root
             (fn
               [cursor owner]
               (reify
                 om/IRender
                 (render [_]
                   (if-let [current-id (:current cursor)]
                     (let [{:keys [view state]} (current-id (:routes cursor))]
                       (dom/div nil
                                (if view
                                  (om/build view state))))))))
             mvvm-state
             {:target target-element
              :path [target]})
            (om/update! mvvm-cursor [target :installed?] true))

          ;; set the current state
          (om/update! mvvm-cursor [target :current] id))
        (log-warn target " couldn't be found. Use <TODO> to suppress the warning.")))))

(defn define-routes
  [target routes]

  ;; TODO - add check for duplicate route ids

  ;; check for new routes.
  (doseq [mroute routes]
    (let [route (:route mroute)]
      (when (and (not= route wildcard-route) (not (contains? (route-list) route)))
        (log-debug "Defining a route for " route)
        (defroute (str route) []
          (log-info "Routing " route)
          (launch-route! route)))))

  ;; save the routes.
  (let [ktarget (keyword target)]
    (doseq [mroute (map #(assoc % :target ktarget) routes)]
      (swap! mvvm-state assoc-in [ktarget :routes (:id mroute)] mroute))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-navigate [event]
  (let [path (.-token event)]
    (secretary/dispatch! path)))

(defonce set-up-history!
  (doto history
    (goog.events/listen EventType/NAVIGATE on-navigate)
    (.setEnabled true)))

(defn on-js-reload []) ;; TODO
