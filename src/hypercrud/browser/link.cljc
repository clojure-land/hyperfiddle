(ns hypercrud.browser.link
  (:require [cats.core :as cats]
            [cats.monad.either :as either]
            [contrib.eval :refer [eval-str]]
            [hypercrud.util.reactive :as reactive]
            [contrib.string :refer [memoized-safe-read-edn-string]]
            [contrib.try :refer [try-either]]
            [hypercrud.browser.base :as base]
            [hypercrud.browser.context :as context]
            [hypercrud.browser.popovers :as popovers]
            [hypercrud.browser.q-util :as q-util]
            [hypercrud.browser.routing :as routing]
            [hypercrud.util.core :refer [pprint-str unwrap]]
            [hyperfiddle.foundation.actions :as foundation-actions]
            [hyperfiddle.runtime :as runtime]
            [promesa.core :as p]
            [taoensso.timbre :as timbre]))


(defn popover-link? [link]
  (:link/managed? link))

(defn same-path-as? [path]
  (fn [link]
    (either/branch
      (memoized-safe-read-edn-string (str "[" (:link/path link) "]"))
      (fn [e]
        (timbre/error e)                                    ; swallow the error
        false)
      #(= path %))))

(defn options-link? [link]
  ; don't care if its inline or not, just do the right thing.
  (= :options (:link/rel link)))

(def options-processor (partial remove options-link?))

(defn options-link [path ctx]
  (->> @(:hypercrud.browser/links ctx)
       (filter (same-path-as? path))
       (filter options-link?)
       first))

(defn rel->link [rel ctx]
  (->> @(:hypercrud.browser/links ctx)
       (filter #(= (:link/rel %) rel))
       first))

; todo belongs in routing ns
; this is same business logic as base/request-for-link
; this is currently making assumptions on dbholes
(defn validated-route' [fiddle route ctx]
  ; We specifically hydrate this deep just so we can validate anchors like this.
  (let [[_ params] route]
    (case (:fiddle/type fiddle)
      ; todo check fe conn
      ; todo merge in dbhole lookup, see: hypercrud.browser.base/request-for-link
      :query (let [q (unwrap (q-util/safe-parse-query-validated fiddle))]
               (base/validate-query-params q params ctx))
      :entity (if (not= nil params)                         ; handles `e` but no logic for `[e a]`
                ; todo check fe conn
                (either/right route)
                (either/left {:message "malformed entity param" :data {:params params}}))
      :blank (either/right route)
      (either/left {:message "route has no fiddle" :data {:route route}}))))

(defn get-or-apply' [expr & args]
  (if (fn? expr)
    (try-either (apply expr args))
    (either/right expr)))

(defn ^:export build-link-props-raw [unvalidated-route' link ctx] ; ctx is for display-mode

  ; this is a fine place to eval, put error message in the tooltip prop
  ; each prop might have special rules about his default, for example :visible is default true, does this get handled here?

  (let [fiddle (:link/fiddle link)                          ; can be nil - in which case route is invalid
        [_ args :as route] (unwrap unvalidated-route')
        validated-route' (validated-route' fiddle route ctx)
        user-props' (cats/bind (eval-str (:hypercrud/props link))
                               (fn [user-expr]
                                 (if user-expr
                                   (get-or-apply' user-expr ctx)
                                   (either/right nil))))
        user-props-map-raw (cats/extract (cats/mplus user-props' (either/right nil)))
        user-prop-val's (map #(get-or-apply' % ctx) (vals user-props-map-raw))
        user-prop-vals (map #(cats/extract (cats/mplus % (either/right nil))) user-prop-val's)
        errors (->> (concat [user-props' unvalidated-route' validated-route'] user-prop-val's)
                    (filter either/left?) (map cats/extract) (into #{}))
        user-props (zipmap (keys user-props-map-raw) user-prop-vals)]
    (merge
      user-props                                            ; e.g. disabled, tooltip, style, class - anything, it gets passed to a renderer maybe user renderer
      ; doesn't handle tx-fn - meant for the self-link. Weird and prob bad.
      {:route (unwrap unvalidated-route')
       :tooltip (if-not (empty? errors)
                  [:warning (pprint-str errors)]
                  (if (:ide-active ctx)
                    [nil (pr-str args)]
                    (:tooltip user-props)))
       :class (->> [(:class user-props)
                    (if-not (empty? errors) "invalid")]
                   (remove nil?)
                   (interpose " ")
                   (apply str))})))

(defn stage! [link route popover-id child-branch ctx]
  (let [user-txfn (or (unwrap (eval-str (:link/tx-fn link))) (constantly nil))]
    (-> (p/promise
          (fn [resolve! reject!]
            (let [swap-fn (fn [multi-color-tx]
                            ; todo why does the user-txfn have access to the parent fiddle's context
                            (let [result (let [result (user-txfn ctx multi-color-tx route)]
                                           ; txfn may be sync or async
                                           (if-not (p/promise? result) (p/resolved result) result))]
                              ; let the caller of this :stage fn know the result
                              ; This is super funky, a swap-fn should not be effecting, but seems like it would work.
                              (p/branch result
                                        (fn [v] (resolve! nil))
                                        (fn [e]
                                          (reject! e)
                                          (timbre/warn e)))

                              ; return the result to the action, it could be a promise
                              result))]
              (runtime/dispatch! (:peer ctx)
                                 (foundation-actions/stage-popover (:peer ctx) (:hypercrud.browser/invert-route ctx) child-branch swap-fn
                                                                   (foundation-actions/close-popover (:branch ctx) popover-id))))))
        ; todo something better with these exceptions (could be user error)
        (p/catch (fn [err]
                   #?(:clj  (throw err)
                      :cljs (js/alert (pprint-str err))))))))

(defn close! [popover-id ctx]
  (runtime/dispatch! (:peer ctx) (foundation-actions/close-popover (:branch ctx) popover-id)))

(defn cancel! [popover-id child-branch ctx]
  (runtime/dispatch! (:peer ctx) (foundation-actions/batch
                                   (foundation-actions/close-popover (:branch ctx) popover-id)
                                   (foundation-actions/discard-partition child-branch))))

(defn managed-popover-body [link route popover-id child-branch dont-branch? ctx]
  [:div.hyperfiddle-popover-body
   ; NOTE: this ctx logic and structure is the same as the popover branch of browser-request/recurse-request
   (let [ctx (-> (if dont-branch? ctx (assoc ctx :branch child-branch))
                 (context/clean))]
     #?(:clj  (assert false "todo")
        :cljs [hypercrud.browser.core/ui-from-route route ctx])) ; cycle
   (when-not dont-branch?
     [:button {:on-click (reactive/partial stage! link route popover-id child-branch ctx)} "stage"])
   ; TODO also cancel on escape
   (if dont-branch?
     [:button {:on-click (reactive/partial close! popover-id ctx)} "close"]
     [:button {:on-click (reactive/partial cancel! popover-id child-branch ctx)} "cancel"])])

(defn open! [route popover-id child-branch dont-branch? ctx]
  (runtime/dispatch! (:peer ctx)
                     (if dont-branch?
                       (foundation-actions/open-popover (:branch ctx) popover-id)
                       (foundation-actions/add-partition (:peer ctx) route child-branch (::runtime/branch-aux ctx)
                                                         (foundation-actions/open-popover (:branch ctx) popover-id)))))

; if this is driven by link, and not route, it needs memoized.
; the route is a fn of the formulas and the formulas can have effects
; which have to be run only once.
(defn build-link-props [link ctx & [dont-branch?]]          ; this 'dont-branch?' argument is a holdover for topnav until 'iframe/button/anchor'
  ; Draw as much as possible even in the presence of errors, still draw the link, collect all errors in a tooltip.
  ; Error states:
  ; - no route
  ; - invalid route
  ; - broken user formula
  ; - broken user txfn
  ; - broken user visible fn
  ; If these fns are ommitted (nil), its not an error.
  (let [route' (routing/build-route' link ctx)
        hypercrud-props (build-link-props-raw route' link ctx)
        popover-props (if (popover-link? link)
                        (if-let [route (and (:link/managed? link) (either/right? route') (cats/extract route'))]
                          ; If no route, there's nothing to draw, and the anchor tooltip shows the error.
                          (let [popover-id (popovers/popover-id link ctx)
                                child-branch (popovers/branch ctx link)]
                            {:showing? (runtime/state (:peer ctx) [::runtime/partitions (:branch ctx) :popovers popover-id])
                             :body [managed-popover-body link route popover-id child-branch dont-branch? ctx]
                             :open! (reactive/partial open! route popover-id child-branch dont-branch? ctx)})))]
    (merge hypercrud-props {:popover popover-props})))
