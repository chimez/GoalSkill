(ns app.renderer.events
  (:require
   [app.renderer.db :refer [default-skills-db levels
                            inc-method-times! get-skills-db
                            update-all-skill-level!]]
    [re-frame.core  :as rf :refer [reg-event-db reg-event-fx inject-cofx path after]]
    [cljs.spec.alpha :as s]))



;; ;; -- Domino 2 - Event Handlers -----------------------------------------------

;; (rf/reg-event-db              ;; sets up initial application state
;;                  :initialize                 ;; usage:  (dispatch [:initialize])
;;                  (fn [_ _]                   ;; the two parameters are not important here, so use _
;;                    {:time (js/Date.)         ;; What it returns becomes the new application state
;;                     :time-color "#f88"}))    ;; so the application state will initially be a map with two keys


;; (rf/reg-event-db                ;; usage:  (dispatch [:time-color-change 34562])
;;                  :time-color-change            ;; dispatched when the user enters a new colour into the UI text field
;;                  (fn [db [_ new-color-value]]  ;; -db event handlers given 2 parameters:  current application state and event (a vector)
;;                    (assoc db :time-color new-color-value)))   ;; compute and return the new application state


;; (rf/reg-event-db                 ;; usage:  (dispatch [:timer a-js-Date])
;;                  :timer                         ;; every second an event of this kind will be dispatched
;;                  (fn [db [_ new-time]]          ;; note how the 2nd parameter is destructured to obtain the data value
;;                    (assoc db :time new-time)))  ;; compute and return the new application state

;; 事件处理函数

;; 初始化事件 [:initialize]
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:skills default-skills-db ; 用户数据库
    :layer-number 1 ; 当前视图层级
    :levels levels ; 等级信息
    :layer-name nil ; 当前层的主题,第一层没有,第二层是家族名,第三层是技能名
    }))

;; 改变显示层级 [:layer-number-change 1]
(rf/reg-event-db
 :layer-number-change
 (fn [db [_ new-layer-number]]
   (assoc db :layer-number new-layer-number)))

;; 点击家族标签,跳入第二层 [:click-family family-name]
(rf/reg-event-fx
 :click-family
 (fn [{:keys [db]} [_ family-name]]
   {:db (assoc db :layer-name family-name)
    :dispatch [:layer-number-change 2]}))
;; 点击技能标签,跳入第三层 [:click-skill skill-name]
(rf/reg-event-fx
 :click-skill
 (fn [{:keys [db]} [_ skill-name]]
   {:db (assoc db :layer-name skill-name)
    :dispatch [:layer-number-change 3]}))

;; 点击方法标签,方法次数+1 [:inc-method skill-name method-name]
(rf/reg-event-db
 :inc-method
 (fn [db [_ skill-name method-name]]
   (inc-method-times! skill-name method-name)
   (update-all-skill-level!)
   {:skills (get-skills-db)
    :layer-number 3
    :levels levels
    :layer-name skill-name}))
