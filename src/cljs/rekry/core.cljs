(ns rekry.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [rekry.events]
              [rekry.subs]
              [rekry.routes :as routes]
              [rekry.views :as views]
              [rekry.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (enable-console-print!)
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
