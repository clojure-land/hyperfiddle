(ns hyperfiddle.ide.fiddles.fiddle-links.renderer
  (:require [cats.monad.either :as either]
            [contrib.reactive :as r]
            [contrib.reagent :refer [from-react-context fix-arity-1-with-context]]
            [hypercrud.browser.base :as base]
            [hypercrud.browser.context :as context]
            [hypercrud.browser.system-fiddle :as system-fiddle]
            [hypercrud.browser.system-link :refer [system-link?]]
            [hypercrud.ui.error :as ui-error]
            [hyperfiddle.data :refer [sort-fn]]
            [hyperfiddle.runtime :as runtime]
            [hyperfiddle.ui :refer [hyper-control field table fiddle]]))

(defn read-only? [ctx]
  (if (:hypercrud.browser/data ctx)                         ; be robust to being called on labels
    (let [entity (get-in ctx [:hypercrud.browser/parent :hypercrud.browser/data])
          sys? (system-link? @(r/fmap :db/id entity))
          shadow? @(r/fmap :hypercrud/sys? entity)]
      (or sys? shadow?))))

(def read-only-cell
  (from-react-context
    (fn [{:keys [ctx props]} value]
      ; Need to delay until we have the value ctx to compute this, which means its a value renderer not a field prop
      [fix-arity-1-with-context                             ; rebind updated props
       (hyper-control ctx) value ctx
       (assoc props :read-only (read-only? ctx))])))

(defn links->result [links]
  (->> @links
       ;(sort-by (juxt :link/disabled :link/rel)
       ;         (fn [[a b] [a' b']] (if (= a a') (< b b') (< a a'))))
       (mapv (fn [link]
               (if (system-fiddle/system-fiddle? (get-in link [:link/fiddle :db/ident]))
                 (dissoc link :link/fiddle)
                 link)))))

(defn renderer [ctx class] nil
  #_(-> (base/data-from-route (:target-route ctx)
                            (assoc ctx
                              :hypercrud.browser/domain @(runtime/state (:peer ctx) [::runtime/domain])
                              :keep-disabled-anchors? true))
      (either/branch
        (fn [e]
          [:div {:class class}
           [(ui-error/error-comp ctx) e]
           (fiddle ctx)])
        (fn [{:keys [:hypercrud.browser/links]}]
          (let [ctx (-> ctx
                        (dissoc :hypercrud.browser/data :hypercrud.browser/data-cardinality :hypercrud.browser/path)
                        (assoc :hypercrud.browser/result (r/track links->result links))
                        (context/focus [:body]))]
            [:div {:class class}
             [table
              #_(partial form (fn [path ctx ?f & args] (field path ctx ?f :read-only (read-only? ctx))))
              (fn [ctx]
                [(field [0 :link/disabled?] ctx nil)
                 (field [0 :link/rel] ctx read-only-cell)
                 (field [0 :link/dependent?] ctx read-only-cell)
                 (field [0 :link/path] ctx read-only-cell)
                 (field [0 :link/render-inline?] ctx nil)
                 (field [0 :link/fiddle] ctx nil)
                 (field [0 :link/create?] ctx read-only-cell)
                 (field [0 :link/managed?] ctx read-only-cell)
                 (field [0 :link/formula] ctx nil)
                 (field [0 :link/tx-fn] ctx nil)
                 (field [0 :hypercrud/props] ctx nil)
                 (field [0] ctx nil)])
              sort-fn
              ctx]])))))
