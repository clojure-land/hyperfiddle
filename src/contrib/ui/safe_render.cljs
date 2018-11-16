(ns contrib.ui.safe-render
  (:require [contrib.cljs-platform :refer [code-for-browser code-for-nodejs]]
            [reagent.core :as reagent]))


(declare user-portal)

(code-for-nodejs
  (defn user-portal [with-error & children]
    ; No portal in SSR, so errors will crash the whole page.
    ; IDE doesn't SSR so use the IDE to fix it.
    (into [:<>] children)))

(code-for-browser
  (defn user-portal [with-error ?class & children]
    (let [show-error (atom false)
          e-state (reagent/atom nil)]
      (reagent/create-class
        {:reagent-render (fn [with-error ?class & children]
                           (let [e @e-state]
                             (if (and @show-error e)
                               (do
                                 (reset! show-error false)  ; only show the error once, retry after that
                                 [with-error e ?class])
                               (into [:<>] children))))

         :component-did-catch (fn [this e info]
                                (reset! show-error true)
                                (reset! e-state e))}))))
