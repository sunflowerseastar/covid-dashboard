(ns covid-dashboard.css
  (:require [garden.def :refer [defkeyframes defstyles]]))

(defkeyframes virion-animation
  [:from {:transform "scale(1.4) translateX(-13%)"}]
  [:to {:transform "scale(1.4) translateX(-17%)"}])

(defstyles screen
  [:#app {:overflow "hidden"}]

  [:.loader {:position "absolute" :top 0 :left 0 :right 0 :bottom 0 :pointer-events "none"}]

  [:.virion-container {:position "absolute"
                       :display "block"
                       :width "23vw"
                       :height "14vw"
                       :padding "2.2vw"
                       :left "50%"
                       :top "50%"
                       :transform "translateX(-50%) translateY(-50%)"
                       :border "1px solid #ddd"}]
  [:.virion-container-inner {:position "relative"
                             :box-sizing "border-box"
                             :width "100%"
                             :height "100%"
                             :padding "20px"
                             :overflow "hidden"}]
  virion-animation
  [:.virion {:position "absolute"
             :display "block"
             :width "100%"
             :left 0
             :top 0
             :animation [[virion-animation "4s" "cubic-bezier(.38,.09,.47,.98)" "forwards"]]}]

  [:.home-page {:height "100vh" :padding "10px" :box-sizing "border-box" :-webkit-font-smoothing "antialiased"}]

  [:h3 :h4 {:margin "0.5em 0 0.4em" :text-align "center"}]
  [:h3 {:font-weight 600 :color "#333" :letter-spacing "0.03em"}]
  [:h4 {:font-weight 400}]
  [:p {:font-size "0.9em" :letter-spacing "0.03em"}]
  [:a {:cursor "pointer"}]
  [:td {:padding "4px 4px 3px 2px"}]

  [:.button {:display "block"
             :width "30px"
             :height "36px"
             :line-height "36px"
             :text-align "center"
             :transition "background 120ms"}]
  [:.button:hover {:background "#f5f5f5"}]
  [:.button:active {:background "#f2f2f2"}]

  [:.home-col-left :.home-col-right {:z-index 1}]

  [:.panel-3-1 {:position "absolute" :left 0 :top 0 :bottom 0 :width "100%"}]

  [:.panel {:background "#fdfdfdd1"}]

  [:.sub-panel-container :.fade {:transition "opacity 2s ease-in-out" :opacity 0}
   [:&.is-active {:opacity "1"}]]

  [:.padding-1 {:box-sizing "border-box" :padding "8px 5px 8px 10px"}]
  [:.padding-2 {:box-sizing "border-box" :padding "8px"}]
  [:.z-index-1 {:z-index 1}]
  [:.bold {:font-weight 600}]
  [:.light {:font-weight 300 :color "#555"}]

  [:.margin-0-auto {:margin "0 auto"}]
  [:.children-align-self-center [:div {:align-self "center"}]]
  [:.text-align-left {:text-align "left"}]
  [:.text-align-right {:text-align "right"}]
  [:.text-align-center {:text-align "center"}]

  [:.graticule {:fill "none"
                :stroke "#eee"
                :stroke-width ".5px"
                :stroke-opacity 0.4}]

  [:.scroll-y-auto {:overflow-y "auto"}]
  [:.svg-container {:position "relative" :top "50%" :transform "translateY(-50%)" :width "100%"
                    :overflow "visible"}])
