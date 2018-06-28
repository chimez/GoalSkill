(ns app.renderer.views
  (:require [reagent.core  :as reagent]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            [clojure.string :as str]))

;; ;; 视图层

;; 通用的显示函数
(defn show-a-card [card_name subtitle text & [event-label event-target]]
  "一个卡片的名称+标题+描述 -> 显示一个卡片"
  [:div (merge {:class (if-not (and (= subtitle nil) (= text nil))
                  "card col"
                  "card col text-center")
                :style {:width "18rem"}}
               (if event-label
                 {:on-click #(rf/dispatch [event-label event-target])}
                 {:on-click (fn [])}))
   [:div {:class "card-body"}
    [:h4 {:class "card-title"} card_name]
    (if-not (= subtitle nil) [:h6 {:class "card-subtitle"} subtitle])
    (if-not (= text nil) [:p {:class "card-text"} text])]])


;; 第一层:显示所有技能家族

(defn all-skill-family-name []
  "返回所有技能族的名称"
  @(rf/subscribe [:all-skill-family-name]))

(defn show-a-skill-family [skill-family-name]
  "技能家族名称->应显示的卡片"
  (show-a-card skill-family-name nil nil :click-family skill-family-name))
(defn show-all-skill-family []
  "显示所有的技能族"
  [:div {:class "row"}
   [:div {:class "col-12 text-center"}
    [:h2 "技能类别"]]
   (map show-a-skill-family (all-skill-family-name))])

;; 第二层:显示某个技能族中的所有技能

(defn all-skill-of-this-tier [family-name tier]
  "技能家族名称+阶序->此阶序的所有技能"
  (let [all-skill-of-family-tier @(rf/subscribe [:all-skill-of-family-tier])
        all-skill-raw (for [key (keys all-skill-of-family-tier)]
             (let [family-it (first (get all-skill-of-family-tier key))
                   tier-it (peek (get all-skill-of-family-tier key))]
               (if (and (= tier-it tier)
                        (= family-it family-name))
                 key :not-this)))
        all-skill (vec (set all-skill-raw))
        skill-filter (filter #(not= :not-this %) all-skill)]
    skill-filter))

(defn all-tier-of-this-family [family-name]
  "技能家族名称+阶序->此阶序的所有技能"
  (let [all-tier-of-family @(rf/subscribe [:all-tier-of-familys])]
    (get all-tier-of-family family-name)))

(defn show-a-skill [skill-name]
  "技能名称->应显示的卡片"
  (let [skill-level-exp @(rf/subscribe [:skill-level-exp])
        levels @(rf/subscribe [:levels])
        skill-level (first (get skill-level-exp skill-name))
        skill-exp (nth (get skill-level-exp skill-name) 1)
        level-exp (get-in levels [skill-level "exp_needed"])]
    (show-a-card skill-name skill-level (str skill-exp "/" level-exp)
                 :click-skill skill-name)))

(defn show-a-tier [skill-family-name tier]
  "技能家族名称+阶序->显示此阶序的所有技能"
  [:div {:class "row"}
   [:div {:class "col-12"}
    [:h4 tier]]
   (map show-a-skill (all-skill-of-this-tier skill-family-name tier))])

(defn show-all-tier [skill-family-name]
  "技能家族名称->按等级分类的所有技能"
  [:div {:class "row"}
   (map (partial show-a-tier skill-family-name)
        (all-tier-of-this-family skill-family-name))])

(defn show-all-skill [family]
  "显示某族中的所有技能"
  [:div {:class "row"}
   [:div {:class "col-12 text-center"}
    [:h2 family]]
   [show-all-tier family]])

;; 第三层:显示某个技能的所有方法

(defn show-a-skill-full [skill-name]
  "技能名称->应显示的卡片"
  (let [skill-level-exp @(rf/subscribe [:skill-level-exp])
        levels @(rf/subscribe [:levels])
        skill-level (first (get skill-level-exp skill-name))
        skill-exp (nth (get skill-level-exp skill-name) 1)
        level-exp (get-in levels [skill-level "exp_needed"])
        level-describe (get-in levels [skill-level "describe"])
        level-name (get-in levels [skill-level "name"])]
    (show-a-card skill-name
                 (str skill-level ":" level-name
                      "(" skill-exp "/" level-exp ")")
                 level-describe)))

(defn all-method-of-this-skill [skill-name]
  "返回这个技能的所有可用方法"
  (let [all-method-of-skill @(rf/subscribe [:all-method-of-skill])
        methods-of-this (get all-method-of-skill skill-name)
        method-map (for [method-it methods-of-this]
                     (merge method-it {"skill_name" skill-name}))]
    method-map))

(defn show-a-method [method-map]
  "显示一个方法行"
  (let [method-name (get method-map "method_name")
        method-exp (get method-map "method_exp")
        method-times (get method-map "method_times")
        skill-name (get method-map "skill_name")
        skill-level-exp @(rf/subscribe [:skill-level-exp])
        need-times (quot (nth (get skill-level-exp skill-name) 2) method-exp)]
    [:li {:class "list-group-item d-flex justify-content-between align-items-center"
          :on-click #(rf/dispatch [:inc-method skill-name method-name])}
    method-name
     [:span {:class "badge badge-primary badge-pill"}
      (str method-times "/" need-times)]]))

(defn show-all-method [skill-name]
  "显示某技能的所有可用方法"
  [:div {:class "row"}
   [:div {:class "col-3"}]
   [:div {:class "col-6 text-center"}
    [show-a-skill-full skill-name]]
   [:div {:class "col-3"}]
   [:div {:class "col-12"}
    [:ul {:class "list-group"}
    (map show-a-method (all-method-of-this-skill skill-name))]]])

;; 导航栏
(defn navbar []
  [:nav {:class "navbar navbar-expand-lg navbar-light bg-light"}
   [:a {:class "navbar-brand" :href "#"
        :on-click #(rf/dispatch [:layer-number-change 1])} "技能树"]])

;; 总ui
(defn ui
  []
  (let [layer-number @(rf/subscribe [:layer-number])
        layer-name @(rf/subscribe [:layer-name])]
    [:div
     [navbar]
     [:div {:class "row"}
      [:div {:class "col"}
       (case layer-number
         1 [show-all-skill-family]
         2 [show-all-skill layer-name]
         3 [show-all-method layer-name])]]]))
