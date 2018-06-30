(ns app.renderer.subs
  (:require [re-frame.core :as rf :refer [reg-sub subscribe]]))

;; 数据查询
;; 当前显示的层级 [:layer-number] -> 1
(rf/reg-sub
 :layer-number
 (fn [db _]
   (:layer-number db)))

;; 当前层的名称 [:layer-name] -> "name"
(rf/reg-sub
 :layer-name
 (fn [db _]
   (:layer-name db)))

;; 查询所有的技能家族名称 [:all-skill-family-name] -> ["a" "b" "c"]
(rf/reg-sub
 :all-skill-family-name
 (fn [db _]
   (let [skills (:skills db)
         all-skill-family-name (for [skill skills]
                                 (get skill "skill_family"))]
     (vec (set all-skill-family-name)))))

;; 查询某家族和某阶序下的所有技能名称
;; [:all-skill-of-family-tier] -> {"skill_name" ["family" tier]}
(rf/reg-sub
 :all-skill-of-family-tier
 (fn [db _]
   (let [skills (:skills db)
         all-keys (for [skill skills]
                    (let [name-it (get skill "skill_name")]
                      name-it))
         all-vals (for [skill skills]
                    (let [tier-it (get skill "skill_tier")
                          family-it (get skill "skill_family")]
                      [family-it tier-it]))]
     (zipmap all-keys all-vals))))

;; 查询某家族有哪些阶序 [:all-tier-of-familys] -> {"family_name" [1 2 3]}
(rf/reg-sub
 :all-tier-of-familys
 (fn [db _]
   (let [skills (:skills db)
         family-tier-pair (for [skill skills]
                            [(get skill "skill_family")
                             (get skill "skill_tier")])
         all-family (vec (set (for [skill skills] (get skill "skill_family"))))
         tier-of-family (for [family all-family]
                          (vec (set (for [a-pair (filter #(= family (first %))
                                                         family-tier-pair)]
                                      (last a-pair)))))
         all-tier-of-family (zipmap all-family tier-of-family)]
     all-tier-of-family)))

;; 查询某技能的等级和当前exp和剩余所需exp
;; [:skill-level-exp] -> {"skill_name" ["skill_level" "skill_exp" "need_exp"]}
(rf/reg-sub
 :skill-level-exp
 (fn [db _]
   (let [skills (:skills db)
         levels (:levels db)
         array-raw (for [skill skills]
              (let [name-it (get skill "skill_name")
                    exp-it (get skill "skill_exp")
                    level-it (get skill "skill_level")
                    next-exp (get-in levels [level-it "exp_needed"])
                    need-exp (- next-exp exp-it)]
                [name-it [level-it exp-it need-exp]]))]
     (reduce merge (map #(hash-map (first %) (last %)) array-raw)))))

;; 查询等级信息 [:levels] -> {"level" {"name" ... "desctibe" ... "exp_needed" ...}}
(rf/reg-sub
 :levels
 (fn [db _]
   (:levels db)))

;; 查询技能的所有方法
;; [:all-method-of-skill]
;; -> {"skill_name" [{"method_name" .. "method_exp" ..
                   ;; "method_times" .. "method_class .."}]}
(rf/reg-sub
 :all-method-of-skill
 (fn [db _]
   (let [skills (:skills db)
         skill-method-pair (for [skill skills]
                             [(get skill "skill_name")
                              (get skill "skill_methods")])
         s-map (reduce merge (map #(hash-map (first %) (last %)) skill-method-pair))]
     s-map)))
