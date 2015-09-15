(ns ^:figwheel-always venue.core
    (:require [cljs.core.async :as async :refer [<! chan put! mult tap]]
              [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [schema.core :as s :include-macros true]
              [secretary.core :as secretary :refer-macros [defroute]])
    (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                     [cljs-log.core :as log])
    (:import goog.History))


(defonce venue-state (atom {}))

;;
(defonce chan-sz 20)
(defonce refresh-ch (chan chan-sz))
(defonce refresh-mult (mult refresh-ch))

(def history (History.))
(def log-prefix "[venue]")

(defn log-debug  [& body] (log/debug  log-prefix " " (apply str body)))
(defn log-info   [& body] (log/info   log-prefix " " (apply str body)))
(defn log-warn   [& body] (log/warn   log-prefix " " (apply str body)))
(defn log-severe [& body] (log/severe log-prefix " " (apply str body)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;

(secretary/set-config! :prefix "#")

(defn fixture-by-id
  [id]
  (->> @venue-state
       (map val)
       (mapcat :fixtures)
       (map (partial apply hash-map))
       (reduce conj)
       id))

(defn route-list
  []
  (->> @venue-state
       (map val)
       (mapcat :fixtures)
       (map second)
       (map :route)
       set))

(defn filter-fixtures
  [location opts]
  (let [{:keys [include-static?] :or {include-static? false}} opts]
    (->> @venue-state
         (map val)
         (mapcat :fixtures)
         (map second)
         (filter #(or (and include-static? (:static %)) (= (:route %) location))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn navigate!
  [fixture-id]

  (let [route (->> fixture-id fixture-by-id :route)]
    (if (:static route)
      (throw (js/Error. "Cannot navigate to a static fixture!")))
    (log-info "Navigating to " route)
    (set! (.. js/document -location -href) (str "#" route))))

(defn start!
  []
  (log-info "Starting up...")
  (secretary/dispatch! js/window.location.hash))

(defn- launch-route!
  [location]
  (let [mvvm-cursor (om/root-cursor venue-state)]
    ;; loop routes applicable to this location
    (doseq [{:keys [target id]} (filter-fixtures location {:include-static? true})]
      (if-let [target-element (. js/document (getElementById (name target)))]
        (do
          ;; if we're not installed, add an om/root
          (when (not (:installed? (get mvvm-cursor target)))
            (log-debug target " does not have an om/root. Installing now...")
            (om/root
             (fn
               [cursor owner]
               (reify
                 om/IWillMount
                 (will-mount [_]
                   (let [refresh-tap (tap refresh-mult (chan chan-sz))]
                     (go-loop []
                       (let [_ (<! refresh-tap)]
                         (om/refresh! owner))
                       (recur))))
                 om/IRender
                 (render [_]
                   (if-let [current-id (:current cursor)]
                     (let [{:keys [view state]} (current-id (:fixtures cursor))]
                       (dom/div nil
                                (if view
                                  (om/build (view) state))))))))
             venue-state
             {:target target-element
              :path [target]})
            (om/update! mvvm-cursor [target :installed?] true))

          ;; set the current state
          (om/update! mvvm-cursor [target :current] id))
        (log-warn target " couldn't be found. Use <TODO> to suppress the warning.")))))

(defn- add-view!
  [{:keys [target view view-model id state route] :as fix}]

  ;; TODO perhaps we do something other than throw here?
  (comment (doseq [{:keys [id]} fixtures]
             (if (fixture-by-id id)
               (throw (js/Error. (str "A route with id " id " already exists!"))))))

  ;; check for new routes.
  ;; FIXME - statics are only brought up by launch-route! this clearly needs to change as no routing means no statics appear.
  (when (not (contains? (route-list) route))
    (log-debug "Defining a route for " route)
    (defroute (str route) []
      (log-info "Routing " route)
      (launch-route! route)))

  ;; save the view
  (let [ktarget (keyword target)
        mfix (-> fix
                   (assoc :target ktarget)
                   (assoc :static false))]
    (swap! venue-state assoc-in [ktarget :fixtures id] mfix)))

(defn- add-static-view!
  [{:keys [target id] :as fixture}]
  (let [ktarget (keyword target)]
    (swap! venue-state assoc-in [ktarget :fixtures id] (-> fixture
                                                (assoc :target ktarget)
                                                (assoc :static true)))))

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

(defn on-js-reload []
  (log-debug "Refreshing views...")
  (put! refresh-ch true))
