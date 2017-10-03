(ns hypercrud.browser.context
  (:require [clojure.string :as string]
            [hypercrud.browser.auto-anchor-formula :refer [auto-entity-dbid]]
            [hypercrud.state.actions.core :as actions]
            [hypercrud.util.branch :as branch]
            [reagent.core :as reagent]))


;;;;;;;;;;;;;;;;;;;;
;
; utility fns
;
;;;;;;;;;;;;;;;;;;;;

(defn ident->database-uri [ident ctx]
  (->> (get-in ctx [:domain :domain/databases])
       (filter #(= (:dbhole/name %) ident))
       first
       :dbhole/uri))

;;;;;;;;;;;;;;;;;;;;
;
; core ctx manipulation fns
;
;;;;;;;;;;;;;;;;;;;;

(defn clean [param-ctx]
  ; why not query-params and all the custom ui/render fns?
  (dissoc param-ctx
          :keep-disabled-anchors?
          :route :relation
          :schemas
          :uri :find-element :schema
          :entity :attribute :value
          :layout :field))

(defn override-domain-dbs [ctx query-params]
  (update-in ctx
             [:domain :domain/databases]
             (fn [domain-dbs]
               (->> query-params
                    (filter (fn [[k _]] (and (string? k) (string/starts-with? k "$"))))
                    (into {})
                    (merge domain-dbs)))))

(defn route [param-ctx route]
  (assoc param-ctx
    :route route
    :code-database-uri (->> (get-in param-ctx [:domain :domain/code-databases])
                            (filter #(= (:dbhole/name %) (:code-database route)))
                            first
                            :dbhole/uri)))

(defn anchor-branch [param-ctx anchor]
  (if (:anchor/managed? anchor)
    ; this auto-entity-dbid call makes no sense, there will be collisions, specifically on index links
    ; which means queries of unrendered modals are impacted, an unnecessary perf cost at the very least
    ; we should run the auto-formula logic to determine an appropriate auto-id fn
    (let [child-id-str (:id (auto-entity-dbid param-ctx))
          branch (branch/encode-branch-child (:branch param-ctx) child-id-str)]
      (assoc param-ctx :branch branch))
    param-ctx))

(defn relation [param-ctx relation]
  (assoc param-ctx :relation relation))

(defn user-with [ctx branch uri tx]
  ; todo custom user-dispatch with all the tx-fns as reducers
  ((:dispatch! ctx) (actions/with branch uri tx)))

(defn find-element [param-ctx fe]
  (let [uri (ident->database-uri (:find-element/connection fe) param-ctx)
        branch (:branch param-ctx)]
    (assoc param-ctx :uri uri
                     :find-element fe
                     :schema (get-in param-ctx [:schemas (:find-element/name fe)])
                     :user-with! (reagent/partial user-with param-ctx branch uri))))

(defn entity [param-ctx entity]
  (assoc param-ctx :owner (if-let [owner-fn (:owner-fn param-ctx)]
                            (owner-fn entity param-ctx))
                   :entity entity))

(defn attribute [param-ctx attribute]
  (assoc param-ctx :attribute (get-in param-ctx [:schema attribute])))

(defn value [param-ctx value]
  (assoc param-ctx :value value))
