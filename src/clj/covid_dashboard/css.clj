(ns covid-dashboard.css
  (:require [garden.def :refer [defkeyframes defstyles]]
            [garden.stylesheet :refer [at-media]]))

;; keep this in sync with static.cljs
;; TODO dry
(def duration-0 "100ms")
(def duration-1 210)
(def duration-2 (* duration-1 1.8))
(def duration-3 (* duration-1 7))

(def control-bar-height "58px")
(def control-bar-height-desktop "36px")

(defkeyframes virion-animation
  [:from {:transform "scale(1.4) translateX(-11%)"}]
  [:to {:transform "scale(1.4) translateX(-14%)"}])

(defstyles screen

  ;; global

  [:#app {:overflow "hidden" :height "100%"}]
  [:.rc-box {:overflow "hidden" :transition (str "all " duration-0 " ease-in-out")}]

  ;; type

  [:h3 :h4 {:margin "0.5em 0 0.4em" :padding "0 0.8em" :color "#333" :text-align "center" :letter-spacing "0.03em"}]
  [:h3 {:font-size "21px" :font-weight 600}]
  [:.desktop [:h3 {:font-size "14px"}]]
  [:h4 {:font-size "19px" :font-weight 300}
   [:&.extra-margin {:margin "2em 0 0.55em"}]]
  [:.desktop [:h4 {:font-size "15px"} [:&.extra-margin {:margin "1em 0 0.55em"}]]]
  [:h5 {:margin 0 :padding "0 0.7em" :color "#333" :font-weight 300 :font-size "10px" :text-align "center"
        :text-transform "uppercase" :letter-spacing "0.03em"}]
  [:.desktop [:h5 {:font-size "9px"}]]
  [:h6 {:margin "0.2em 0 0.7em" :padding "0 0.8em" :color "#777" :font-weight 400 :font-size "14px" :text-align "center"}]
  [:.desktop [:h6 {:font-size "12px" :color "#666"}]]
  [:p {:font-size "0.9em" :letter-spacing "0.03em"}]
  [:a {:cursor "pointer"}]
  [:a.information-link {:position "relative" :top "-3px" :display "inline-block" :margin-top "2.4em"
                        :font-size "12px" :font-weight 300 :color "#666" :text-align "center"
                        :border-bottom "1px solid #ccc" :transition "all 120ms"}
   [:&:hover {:color "#333" :border-bottom "1px solid #aaa"}]]
  [:.desktop [:a.information-link {:margin "6px 0" :font-size "10px"}]]

  ;; html

  [:table {:padding "0 1em"}]
  [:.tablet [:table {:padding "0em"}]]
  [:.small-monitor [:table {:padding "0.4em"}]]
  [:.large-monitor [:table {:padding "0.6em"}]]
  [:td {:padding "4px 6px 3px 3px"
        :font-size "15px"}]
  [:.desktop [:td {:padding "4px 4px 3px 2px" :font-size "12px"}]]

  ;; buttons

  [:.button {:display "block" :width "80px" :height control-bar-height :font-size "21px" :color "#ccc" :text-shadow "1px 1px 0 #fff"
             :line-height control-bar-height :text-align "center" :background "#f4f4f4" :transition "background 120ms"}]
  [:.desktop [:.button {:width "30px" :height control-bar-height-desktop :line-height control-bar-height-desktop
                        :font-size "12px" :color "#aaa" :background "#f8f8f8"}]]
  [:.button:hover {:background "#f5f5f5"}]
  [:.button:active {:background "#f2f2f2"}]


  ;; loader

  [:.loader {:position "absolute" :top 0 :left 0 :right 0 :bottom 0 :pointer-events "none"}]
  [:.virion-container {:position "absolute" :display "block" :width "44vw" :height "28vw"
                       :padding "14px" :left "50%" :top "50%"
                       :transform "translateX(-50%) translateY(-50%)" :border "1px solid #ddd"}
   (at-media {:min-width "580px"} [:& {:padding "16px" :width "36vw" :height "22vw"}])
   (at-media {:min-width "768px"} [:& {:padding "18px" :width "21vw" :height "12.5vw"}])
   (at-media {:min-width "960px"} [:& {:padding "22px"}])
   (at-media {:min-width "1200px"} [:& {:padding "26px"}])
   (at-media {:min-width "1600px"} [:& {:padding "34px"}])]
  [:.virion-container-inner {:position "relative" :box-sizing "border-box" :width "100%" :height "100%" :overflow "hidden"}]
  virion-animation
  [:.virion {:position "absolute" :display "block" :width "100%" :left 0 :top 0
             :animation [[virion-animation "4s" "cubic-bezier(.38,.09,.47,.98)" "forwards"]]}]

  ;; layout

  [:.home-page {:height "100vh" :padding "10px" :box-sizing "border-box" :-webkit-font-smoothing "antialiased"}]
  [:.control-bar-height-box {:height control-bar-height
                             :border "1px solid blue"
                             :flex (str "1 1 " control-bar-height)}]
  [:.home-col-left :.home-col-right {:z-index 1}]

  ;; panel

  [:.panel {:background "#fdfdfdd1"}]

  [:.detail {:font-weight 300}]
  [:.detail-table {:margin "0 auto 0.6em"}
   [:td {:font-size "17px" :line-height 1.2}]
   [:td:first-child {:text-align "right"}]]

  [:.menu-container {:background "#fffe" :border-top "1px solid #eee" :z-index 2}]
  [:.desktop [:.menu-container {:bottom control-bar-height-desktop}]]
  [:ul.menu {:margin 0 :padding-left 0 :list-style "none"}
   [:li {:padding "10px" :cursor "pointer" :transition (str "opacity " duration-2 "ms ease-in-out")
         :border-left "1px solid #eee" :border-right "1px solid #eee"}
    [:&:first-child :padding-top "18px"]
    [:&:last-child :padding-bottom "12px"]
    [:&:hover {:background "#f9f9f9"}]
    [:&.is-selected {:background "#f3f3f3" :cursor "default"}]]
   [:li+li {:border-top "1px dotted #eee"}]
   [:li:last-child {:border-bottom "1px dotted #eee"}]]
  [:.white-fade-bottom {:position "relative"}
   [:&:after {:content "''" :position "absolute" :bottom 0 :left 0
              :width "100%" :height "12px"
              :background-image "linear-gradient(#fff0, #ffff)"
              :pointer-events "none" :overflow "visible" :z-index 2}]]

  ;; visualizations

  [:.svg-container {:position "relative" :top "50%" :transform "translateY(-50%)" :width "100%" :height "100%" :overflow "visible"}]
  [:.svg-pointer-events-none [:svg {:pointer-events "none"}]]
  [:.graticule {:fill "none" :stroke "#eee" :stroke-width ".5px" :stroke-opacity 0.4}]
  [:#line-chart-root-svg {:height "200px"}]

  ;; utility

  [:.fade-duration-3 {:transition (str "opacity " duration-3 "ms ease-in-out") :opacity 0}]
  [:.fade-duration-2 {:transition (str "opacity " duration-2 "ms ease-in-out") :opacity 0}]
  [:.is-active {:opacity "1"}]
  [:.is-inactive {:pointer-events "none !important"}]

  [:.padding-1 {:box-sizing "border-box" :padding "8px 10px"}]
  [:.padding-2 {:box-sizing "border-box" :padding "8px"}]
  [:.padding-3-top {:box-sizing "border-box" :padding-top "2px"}]
  [:.z-index-1 {:z-index 1}]
  [:.bold {:font-weight 600}]
  [:.light {:font-weight 300 :color "#555"}]

  [:.u-width-100 {:width "100%"}]
  [:.u-height-100 {:height "100%"}]
  [:.u-position-relative {:position "relative"}]
  [:.u-absolute-all {:position "absolute" :left 0 :right 0 :top 0 :bottom 0}]
  [:.margin-0-auto {:margin "0 auto"}]
  [:.children-align-self-center [:div {:align-self "center"}]]
  [:.text-align-left {:text-align "left"}]
  [:.text-align-right {:text-align "right"}]
  [:.text-align-center {:text-align "center"}]
  [:.justify-content-center {:justify-content "center"}]
  [:.cursor-pointer {:cursor "pointer"}]
  [:.pointer-events-none {:pointer-events "none"}]
  [:.pointer-events-auto {:pointer-events "auto"}]
  [:.scroll-y-auto {:overflow-y "auto" :-webkit-overflow-scrolling "touch"}])
