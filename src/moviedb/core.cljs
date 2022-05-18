(ns moviedb.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [ajax.core :refer [GET json-response-format]]))

;; -------------------------
;; Views

(def movie-title (r/atom ""))
(def omdb-resp (r/atom ""))

(defn handle-omdb-resp [resp]
  (.log js/console "Response:" (:Title resp) (:Year resp))
  (reset! omdb-resp resp)
  (swap! omdb-resp assoc :loaded true))

(defn home-page []
  [:div {:class "home-container"}
   [:h2 "Enter a movie title:"]
   [:form {:on-submit (fn [e] 
                        (.preventDefault e)
                        (GET "http://www.omdbapi.com"
                          {:params {:apikey "a74b26be" :t @movie-title}
                           :handler handle-omdb-resp
                           :response-format (json-response-format {:keywords? true})}))}
    [:input {:type "input"
             :on-change #(reset! movie-title (.-value (.-target %)))
             }]]
   (when (:loaded @omdb-resp)
     [:<>
      [:img {:src (:Poster @omdb-resp)}]
      [:h2 (str "Year: " (:Year @omdb-resp))]
      [:h4 (:Plot @omdb-resp)]])])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
