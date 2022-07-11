(ns moviedb.data
  (:require [re-frame.core :as rf]
            [ajax.core :refer [GET json-response-format]]))

(def watchmode-api-key "FK5oCEQDEjfxYb9MeHzFiyFk4QIWL62RpckYIV3A")

(rf/reg-event-fx
 :make-omdb-req
 (fn [db [_ title]]
   {:omdb-req {:url "http://www.omdbapi.com"
               :params {:apikey "a74b26be" :t title}
               :handler #(rf/dispatch [:handle-omdb-resp %])
               :response-format (json-response-format {:keywords? true})}}))

(rf/reg-fx 
 :omdb-req
 (fn [req]
   (GET (:url req) req)))

(rf/reg-sub
 :omdb-resp
 (fn [db]
   (get-in db [:omdb-data])))

(rf/reg-event-fx ;; handle multiple side effects
 :handle-omdb-resp
 (fn [db [_ resp]]
   {:db (update db :omdb-data assoc :resp resp :loaded true) ;; same as reg-event-db
    :wm-req {:url (str ;; parameters for reg-fx handler
                   "https://api.watchmode.com/v1/title/" 
                   (:imdbID resp) 
                   "/sources/?apiKey=" 
                   watchmode-api-key)
             :handler #(rf/dispatch [:save-wm-resp %])
             :response-format (json-response-format {:keywords? true})}}))

(rf/reg-fx ;; handler for the wm-req side effect
 :wm-req
 (fn [wm-req]
   (GET (:url wm-req) wm-req)))

(rf/reg-event-db
 :save-wm-resp
 (fn [db [_ resp]]
   (assoc db :wm-data resp)))

(rf/reg-sub
 :wm-resp
 (fn [db]
   (get-in db [:wm-data])))
