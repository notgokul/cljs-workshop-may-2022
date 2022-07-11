(ns moviedb.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [moviedb.data :as data]
   [re-frame.core :as rf]))

;; -------------------------
;; Views

(def movie-title (r/atom ""))

(defn streaming-card [data]
  [:div {:style {:margin "5px"
                 :border "1px solid white"
                 :padding "10px"}}
   [:a {:href (:web_url data)
        :target "_blank"}
   [:p {:style {:font-size "25px"}} (:name data)]]
   [:p {:style {:font-size "15px"}} (:format data)]
   [:p {:style {:color "green"}} (:price data)]])

(defn home-page []
  [:div {:class "home-container"}
   [:h2 "Enter a movie title:"]
   [:form {:on-submit (fn [e]
                        (.preventDefault e)
                        (rf/dispatch [:make-omdb-req @movie-title]))}
    [:input {:type "input"
             :on-change #(reset! movie-title (.-value (.-target %)))}]]
   (let [omdb-resp (rf/subscribe [:omdb-resp])
         omdb-data (:resp @omdb-resp)
         wm-resp (rf/subscribe [:wm-resp])]
     [:<>
      (when (:loaded @omdb-resp)
        [:<>
         [:img {:src (:Poster omdb-data)}]
         [:h2 (str "Year: " (:Year omdb-data))]
         [:h4 (:Plot omdb-data)]])
      (when @wm-resp
        [:div {:style {:display "flex"
                       :flex-wrap "wrap"}}
         (map (fn [streaming-data]
                [streaming-card streaming-data]) @wm-resp)])])])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
