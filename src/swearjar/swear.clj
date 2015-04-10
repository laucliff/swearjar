(ns swearjar.swear
  (:require [clojure.string :as string]))

(def pottymouths (ref []))

(defn record-swear [nick]
  (dosync
   (if-let [pottymouth (some #(when (= nick (get % :name)) %) @pottymouths)]
    ;Can't I just update by reference? (using pottymouth)
      (let [index (.indexOf @pottymouths pottymouth)]
      (alter pottymouths update-in [index :count] inc))
      (let [new-pottymouth {:name nick :count 1}]
        (alter pottymouths conj new-pottymouth)))))

(def swear-exp #"(butt|piss)+")

(defn is-swear? [word] (not (nil? (re-matches swear-exp word))))

(defn reprimand-user [user])

(defn check-swear [user word]
  (if (is-swear? word)
    ;fancy way is to use juxt?
    ;(do (record-swear user) (reprimand-user user))
    ((juxt record-swear reprimand-user) user)
    ))

(defn digest-words [nick words]
  (doall
   (map (partial check-swear nick) words)))


(defn stringify-record [record]
  (let [name (get record :name)
        count (get record :count)]
    (str name ": " count)))

(defn stringify-pottymouths []
  (let [sorted-pottymouths (sort-by :count > @pottymouths)]
    (concat ["TOP POTTYMOUTHS"] (map stringify-record sorted-pottymouths))))




;next steps:
;do reprimand
