(ns rekry.db)

(def default-db
  {:name "re-frame"
   :nick nil
   :nick-sugg nil
   :nick-available false
   :nick-info-text ""
   :messages []
   :message ""
   :channels ["Main" "Funny" "Random" "Serious"]
   :joined-channels #{"Main"}
   :chosen-channel "Main"
   :max-id 0})

