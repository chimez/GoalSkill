(ns app.renderer.events
  (:require
   [app.renderer.db :refer [get-skills-db levels
                            inc-method-times! get-skills-db
                            update-all-skill-level! destroy-db!
                            reset-all-skill-methods! db-path
                            fs]]
    [re-frame.core  :as rf :refer [reg-event-db reg-event-fx inject-cofx path after]]
    [cljs.spec.alpha :as s]))


;; 事件处理函数

;; 初始化事件 [:initialize]
(rf/reg-event-db
 :initialize
 (fn [_ _]
   (let [db-exists (.pathExists fs db-path)]
     (.then db-exists (fn [exist]
                        (when-not exist
                          (get-skills-db)
                          (reset-all-skill-methods!)))))
   (println "initialize!")
   {:skills (get-skills-db) ; 用户数据库
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

;; 删除数据库,重新初始化
(rf/reg-event-fx
 :destroy-db
 (fn [{:keys [db]} _]
   (destroy-db!)
   {:dispatch [:initialize]}))
