(ns hypercrud.ui.select
  (:require [cats.monad.exception :as exception]
            [hypercrud.client.tx :as tx]
            [hypercrud.form.option :as option]
            [hypercrud.types :refer [->DbId]]
            [hypercrud.browser.connection-color :as connection-color]))


(defn select-boolean* [value props param-ctx]
  (let [props {;; normalize value for the dom - value is either nil, an :ident (keyword), or eid
               :value (if (nil? value) "" (str value))
               ;; reconstruct the typed value
               :on-change #(let [v (case (.-target.value %)
                                     "" nil
                                     "true" true
                                     "false" false)]
                             ((:user-swap! param-ctx) {:tx (tx/update-entity-attr (:entity param-ctx) (:attribute param-ctx) v)}))
               :disabled (if (:read-only props) true false)}]
    [:select props
     [:option {:key true :value "true"} "True"]
     [:option {:key false :value "false"} "False"]
     [:option {:key :nil :value ""} "--"]]))


(defn select* [value options-anchor props param-ctx]
  ; value :: {:db/id #DbId[17592186045891 17592186045422]}
  (let [props {;; normalize value for the dom - value is either nil, an :ident (keyword), or eid
               :value (cond
                        (nil? value) ""
                        :else (-> value :db/id :id str))

               ;; reconstruct the typed value
               :on-change #(let [select-value (.-target.value %)
                                 dbid (cond
                                        (= "" select-value) nil
                                        :else-hc-select-option-node (->DbId (js/parseInt select-value 10) (-> value :db/id :conn-id)))] ;todo bugfix, value can be null
                            ((:user-swap! param-ctx) {:tx (tx/update-entity-attr (:entity param-ctx) (:attribute param-ctx) dbid)}))
               :disabled (:read-only props)}
        options (option/hydrate-options options-anchor param-ctx)]
    [:span.select
     (let [option-records (exception/extract options nil)
           _ (when (exception/failure? options)
               ; todo something better with this exception
               (.error js/console (pr-str (.-e options))))
           no-options? (empty? option-records)
           props (update props :disabled #(or % no-options?))
           props (if (#{:find-element/connection :link-entity/connection :dbhole/value :hypercrud/owner} (-> param-ctx :attribute :attribute/ident)) ; lol hack
                   (assoc props :style {:background-color (connection-color/connection-color (-> value :db/id :id))})
                   props)
           ; hack in the selected value if we don't have options hydrated?
           ; Can't, since we only have the #DbId hydrated, and it gets complicated with relaton vs entity etc
           ]
       [:select.select props
        (concat
          (->> (sort-by second option-records)
               (mapv (fn [[dbid label]]
                       ^{:key dbid}
                       [:option {:value (.-id dbid)} label])))
          [[:option {:key :blank :value ""} "--"]])])]))
