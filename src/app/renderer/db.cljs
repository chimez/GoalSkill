(ns app.renderer.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [clojure.string :as str]
            ))


;; ;; -- Domino 1 - Event Dispatch -----------------------------------------------

;; (defn dispatch-timer-event
;;   []
;;   (let [now (js/Date.)]
;;     (rf/dispatch [:timer now])))  ;; <-- dispatch used

;; ;; Call the dispatching function every second.
;; ;; `defonce` is like `def` but it ensures only one instance is ever
;; ;; created in the face of figwheel hot-reloading of this file.
;; (defonce do-timer (js/setInterval dispatch-timer-event 1000))

(println "Aaaaaaaaaaaaaaaaaaaaaaaa")
;; 定义数据结构和读写数据库的函数,只有这里有副作用
;; 初始化储存位置
(def electron       (js/require "electron"))
(def remote            (.-remote electron))
(def store-path (.getPath remote.app "userData"))

;; 获取静态资源
(def low (js/require "lowdb"))
(def FileSync (js/require "lowdb/adapters/FileSync"))

;; levels
(def adapter-levels (FileSync. (str js/__dirname "/json/levels.json")))
(def db-levels (low adapter-levels))
(def levels (js->clj (.value (.get db-levels "levels"))))

;; learn-methods: reading-methods, solving-methods, thinking-methods, language-methods
(def adapter-learn-methods (FileSync. (str js/__dirname "/json/methods.json")))
(def db-learn-methods (low adapter-learn-methods))
(def learn-methods (.get db-learn-methods "methods"))

;; skills-vectors,默认数据
(def adapter-skills (FileSync. (str js/__dirname "/json/skills.json")))
(def db-skills (low adapter-skills))
(def skills (js->clj (.value (.get db-skills "skills"))))
;; (println (-> db-skills
;;               (.get "skills")
;;               ;; (.find #js{:skill_tier 1})
;;               (.value)))

;; 用户数据库 初始化 这里的数据库是保存在磁盘中的,不是应用的内存数据库
(def fs (js/require "fs-extra"))
(def adapter (FileSync. (str store-path "/db.json")
                        #js{:defaultValue #js{:skills (clj->js skills)}}))
(def db (low adapter))
(def default-skills-db (js->clj (-> db
                             (.get "skills")
                             (.value))))
(defn get-skills-db [] (js->clj (-> db
                                 (.get "skills")
                                 (.value))))

;; 更新数据库
(defn inc-method-times! [skill-name method-name]
  "技能名+方法名->该方法次数+1,该技能经验=+方法经验"
  (let [skill (-> db
                  (.get "skills")
                  (.find #js{:skill_name skill-name}))
        method-obj (-> skill
                       (.get "skill_methods")
                       (.find #js{:method_name method-name}))
        method-times (-> method-obj
                         (.get "method_times")
                         (.value))
        method-exp (-> method-obj
                       (.get "method_exp")
                       (.value))
        skill-exp (-> skill
                      (.get "skill_exp")
                      (.value))]
    (-> method-obj
        (.set "method_times" (inc method-times))
        (.write))
    (-> skill
        (.set "skill_exp" (+ skill-exp method-exp))
        (.write))))

(defn update-skill-level! [skill-name]
  "技能名称->更新该技能的等级,从等级列表中查询"
  (let [skill (-> db
                  (.get "skills")
                  (.find #js{:skill_name skill-name}))
        skill-exp (-> skill
                      (.get "skill_exp")
                      (.value))
        skill-methods (-> skill
                          (.get "skill_methods")
                          (.value))
        level-exp-name-array (for [level-name (keys levels)]
                           [(-> levels (get level-name)
                                           (get "exp_needed")) level-name])
        level-exp-name-map (map #(hash-map (first %1) (first (rest %1)))
                                level-exp-name-array)
        level-exp-name-map-sorted  (into (sorted-map-by <) level-exp-name-map)
        this-level-exp (first (vec
                              (peek (split-with (partial > skill-exp)
                                                 (keys level-exp-name-map-sorted)))))
        this-level-name (get level-exp-name-map-sorted this-level-exp)]
    (-> skill
        (.set "skill_level" this-level-name)
        (.write))))

(defn update-all-skill-level! []
  "更新所有技能的等级,用于等级算法有调整时"
  (let [skills (js->clj  (-> db
                             (.get "skills")
                             (.value)))
        skill-name-array (for [skill skills]
                           (get skill "skill_name"))]
    (doseq [skill-name skill-name-array]
      (update-skill-level! skill-name))))

(defn update-skill-exp! [skill-name]
  "技能名称->更新当前技能经验,由所有的方法次数和经验算得,用于方法经验变动时"
  (let [skill (-> db
                  (.get "skills")
                  (.find #js{:skill_name skill-name}))
        skill_methods (js->clj (-> skill
                          (.get "skill_methods")
                          (.value)))
        method-exp-array (for [method skill_methods]
                           (get method "method_exp"))
        method-times-array (for [method skill_methods]
                             (get method "method_times"))
        new_exp  (reduce + (map * method-times-array method-exp-array))]
    (-> skill
        (.set "skill_exp" new_exp)
        (.write))))

;; (update-all-skill-level!)
;; (update-skill-exp! "英语")

;; (inc-method-times! "英语" "背下来一个单词")

























