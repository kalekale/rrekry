(ns rekry.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :nick-info-text
 (fn [db]
   (:nick-info-text db)))

(re-frame/reg-sub
 :nick-available
 (fn [db]
   (:nick-available db)))

(re-frame/reg-sub
 :nick
 (fn [db _]
   (:nick db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :messages
 (fn [db _]
   (:messages db)))

(re-frame/reg-sub
 :message
 (fn [db _]
   (:message db)))

(re-frame/reg-sub
 :channels
 (fn [db _]
   (:channels db)))

(re-frame/reg-sub
 :chosen-channel
 (fn [db _]
   (:chosen-channel db)))

(re-frame/reg-sub
 :joined-channels
 (fn [db _]
   (:joined-channels db)))
