(ns fixtures.tank
  (:require
    [contrib.reader]))


; These can be copy pasted from hyperfiddle gui
; Don't forget the fiddle ontology

(def fiddles
   {:tutorial.race/submission
    [{:fiddle/ident :tutorial.race/submission
      :fiddle/type :query
      :fiddle/query
      (pr-str
         '[:find
           [(pull ?e [:dustingetz.reg/email
                      :dustingetz.reg/name
                      {:dustingetz.reg/gender [:db/ident]}
                      {:dustingetz.reg/shirt-size [:db/ident]}
                      :db/id])
            ...]
           :where
           [?e :dustingetz.reg/email]])}
     [{:db/id 17592186046196,
       :dustingetz.reg/email "dustin@example.com",
       :dustingetz.reg/name "Dustin Getz",
       :dustingetz.reg/gender {:db/ident :dustingetz.gender/male},
       :dustingetz.reg/shirt-size {:db/ident :dustingetz.shirt-size/mens-large}}
      {:db/id 17592186046763,
       :dustingetz.reg/email "bob@example.com",
       :dustingetz.reg/gender {:db/ident :dustingetz.gender/male},
       :dustingetz.reg/shirt-size {:db/ident :dustingetz.shirt-size/mens-large}}
      #_{:db/id 17592186046764, :dustingetz.reg/email "charlie@example.com", :dustingetz.reg/gender {:db/ident :dustingetz.gender/male}}
      #_{:db/id 17592186046317,
         :dustingetz.reg/email "alice@example.com",
         :dustingetz.reg/name "Alice",
         :dustingetz.reg/gender {:db/ident :dustingetz.gender/female},
         :dustingetz.reg/shirt-size {:db/ident :dustingetz.shirt-size/womens-medium}}
      #_{:db/id 17592186046765,
         :dustingetz.reg/email "elizabeth@example.com",
         :dustingetz.reg/gender {:db/ident :dustingetz.gender/female},
         :dustingetz.reg/shirt-size {:db/ident :dustingetz.shirt-size/womens-medium}}]]

    :tutorial.race/shirt-sizes
    [{:db/id 17592186046202,
      :fiddle/ident :tutorial.race/shirt-sizes,
      :fiddle/links
      [{:db/id 17592186046318,
        :link/class [:hf/iframe],
        :link/fiddle
        {:db/id 17592186046200,
         :fiddle/ident :tutorial.race/genders,
         :fiddle/query
         "[:find\n [(pull ?e [:db/ident]) ...]\n :where\n [?e :db/ident ?i]\n [(namespace ?i) ?ns]\n [(ground \"dustingetz.gender\") ?ns]]",
         :fiddle/type :query},
        :link/path ":dustingetz.reg/gender",
        :link/rel :hf/iframe}],
      :fiddle/query
      "[:find\n [(pull ?e [:db/ident {:dustingetz.reg/gender [:db/ident]}]) ...]\n :in $ ?gender\n :where\n [?e :db/ident ?i]\n [?e :dustingetz.reg/gender ?gender]\n [(namespace ?i) ?ns]\n [(ground \"dustingetz.shirt-size\") ?ns]]",
      :fiddle/renderer
      "(let [{:keys [:hypercrud.browser/fiddle]} ctx]\n  [:div props\n   [hyperfiddle.ui/markdown (:fiddle/markdown @fiddle) ctx]\n   [hyperfiddle.ui/table \n    (fn [ctx]\n      [[hyperfiddle.ui/field [:db/ident] ctx hyperfiddle.ui/hyper-control]\n       [hyperfiddle.ui/field [:dustingetz.reg/gender] ctx hyperfiddle.ui/hyper-control\n        {:options :tutorial.race/genders}]]) \n    ctx]])",
      :fiddle/type :query,
      :hyperfiddle/owners
      [#uuid "acd054a8-4e36-4d6c-a9ec-95bdc47f0d39"]}
     [{:db/ident :dustingetz.shirt-size/womens-medium, :dustingetz.reg/gender {:db/ident :dustingetz.gender/female}}
      {:db/ident :dustingetz.shirt-size/womens-small, :dustingetz.reg/gender {:db/ident :dustingetz.gender/female}}
      {:db/ident :dustingetz.shirt-size/womens-large, :dustingetz.reg/gender {:db/ident :dustingetz.gender/female}}]]

    :hfnet.tank/index
    [{:db/id 17592186045830,
      :fiddle/css
      "table { width: 100%; }\n.p { max-width: 35em; }\n\nfigcaption.figure-caption { /* FIXME WHEN LANDS */\n  text-align: left;\n  font-size: 1.5em;\n}\n\nfigure img {\n  border: 1px solid lightgray;\n}\n\n.highlight { \n  background-color: #ffa; \n}",
      :fiddle/ident :hfnet.tank/index,
      :fiddle/markdown
      "!block{style=\"margin-bottom: 2em;\"}\n\n# Hyperfiddle Tank\n\nThis is a pastebin like environment for learning Hyperfiddle. \n\n1. Please namespace :idents with your username\n1. Schema changes are allowed\n1. To make a fiddle interactive, change !span[tank]{.i}.hyperfiddle.net to !span[demo]{.i}.hyperfiddle.net \n\n!block{style=\"margin-bottom: 2em;\"}\n\n### Showcase\n* [:clojurians](//demo.hyperfiddle.net/:clojurians/)\n* [:seattle/neighborhoods](//demo.hyperfiddle.net/:seattle!neighborhoods/)\n* [gender-shirtsize](//demo.hyperfiddle.net/:dustingetz!gender-shirtsize/)\n* [:tutorial.blog](//demo.hyperfiddle.net/:tutorial.blog/)\n* [markdown-pipeline](//demo.hyperfiddle.net/:dustingetz!markdown-pipeline/#:hf.src!markdown)\n\n!block{style=\"margin-bottom: 2em;\"}\n",
      :fiddle/query
      "[:find \n (pull ?e [:db/id :fiddle/ident])\n (max ?tx)\n ?entrypoint\n :where\n [?e :fiddle/ident]  \n [(hyperfiddle.query/entrypoint-fiddle? $ ?e) ?entrypoint]\n (not [?e :dustingetz.post/hidden true])\n [?e _ _ ?tx]]",
      :fiddle/renderer
      "(let [show-all (reagent.core/atom false)\n      needle (reagent.core/atom \"\")\n      regex? (reagent.core/atom false)]\n  (fn [val ctx props]\n    (let [[err re] (try [nil (re-pattern @needle)]\n                     (catch :default e\n                       [e #\"\"]))]\n      [:main.container props\n       [hyperfiddle.ui/markdown (-> ctx :hypercrud.browser/fiddle deref :fiddle/markdown) ctx]\n       [:h3 \"Recently modified fiddles\"]\n       #_[contrib.ui/easy-checkbox-boolean \"Show all fiddles? \" show-all]\n       [:div.form-group {:class (when (and @regex? err) \"has-error\")}\n        #_[:label.col-sm-2.control-label {:for \"hyperfiddle/entry-point-needle\"} \"Filter\"]\n        [:input.col-sm-10.form-control\n         {:id \"hyperfiddle/entry-point-needle\"\n          :style {:display \"inline-block\"} ; bootstrap styles are half imported, wonky hacks\n          :type \"text\"\n          :on-change #(reset! needle (.. % -target -value))\n          :auto-complete \"off\"\n          #_#_:auto-focus true  ; page sroll pos gets lost, otherwise this is great\n          :placeholder \"filter\"}]\n        #_[:span.col-sm-3\n           [contrib.ui/easy-checkbox-boolean \"Regex? \" regex?]]]\n       (when (and @regex? err) [:span (ex-message err)])\n\n       [:hr]\n\n       [:div.scroll-wrapper\n        [:table.table-hover\n         [:tbody\n          (->> @(:hypercrud.browser/data ctx)\n               #_((if @show-all identity (partial filter #(-> % :fiddle/ident namespace nil?))))\n               (filter (fn [[fiddle tx]]\n                         (let [s (str (:fiddle/ident fiddle))]\n                           (if @regex?\n                             (re-seq re s)\n                             (cuerdas.core/includes? s @needle)))))\n               (sort (fn [[a b] [a' b']]\n                       (if (= b b') \n                         (compare (clojure.string/replace (str (:fiddle/ident a)) #\"/\" \".\")\n                                  (clojure.string/replace (str (:fiddle/ident a')) #\"/\" \".\"))\n                         (> b b'))\n                       ))\n               (map (fn [[fiddle tx is-entrypoint]]\n                      [:tr {:key (str (hash (:fiddle/ident fiddle)))}\n                       [:td\n                        (if is-entrypoint\n                          [hyperfiddle.ui/anchor ctx {:route [(:fiddle/ident fiddle)]} (str (:fiddle/ident fiddle))]\n                          (str (:fiddle/ident fiddle)))\n                        ]]))\n               (doall))]]]])))",
      :fiddle/type :query,
      :hyperfiddle/owners
      [#uuid "acd054a8-4e36-4d6c-a9ec-95bdc47f0d39"]}
     [[{:db/id 17592186045419, :fiddle/ident :karl.hardenstine/fiddles} 13194139534712 true]
      [{:db/id 17592186045426, :fiddle/ident :khardenstine/fiddles} 13194139534711 true]
      [{:db/id 17592186047604, :fiddle/ident :bdevel/post-show} 13194139536499 false]
      [{:db/id 17592186047615, :fiddle/ident :dustingetz/datomic-pull-tx} 13194139536512 true]
      [{:db/id 17592186047618, :fiddle/ident :fsbl0/app} 13194139536533 true]
      [{:db/id 17592186047663, :fiddle/ident :dustingetz/datomic-find-specs} 13194139536562 true]]]

    :hyperfiddle/ide
    [{:db/id 17592186061847,
      :fiddle/renderer
      "hyperfiddle.ide.fiddles.fiddle-src/fiddle-src-renderer",
      :fiddle/links
      [{:db/id 17592186061848,
        :link/class [:hf/remove],
        :link/rel :hf/remove}
       {:db/id 17592186061849,
        :link/class [:hf/remove],
        :link/path ":fiddle/links",
        :link/rel :hf/remove}
       {:db/id 17592186061850,
        :link/class [:hf/iframe],
        :link/fiddle
        {:db/id 17592186045605,
         :fiddle/ident
         :hyperfiddle.ide/fiddle-options,
         :fiddle/query
         "[:find (pull ?link [:db/id :fiddle/ident])\n :where (or [?link :fiddle/ident] [?link :fiddle/type])]",
         :fiddle/type :query},
        :link/rel :hf/iframe}
       {:db/id 17592186061851,
        :link/class [:hf/new],
        :link/fiddle
        {:db/id 17592186056398,
         :fiddle/ident :hyperfiddle.ide/new-fiddle,
         :fiddle/type :entity},
        :link/path ":link/fiddle",
        :link/rel :hf/affix}
       {:db/id 17592186061852,
        :link/class [:hf/new],
        :link/fiddle
        {:db/id 17592186058175,
         :fiddle/ident :hyperfiddle.ide/new-link,
         :fiddle/type :entity},
        :link/path ":fiddle/links",
        :link/rel :hf/affix}],
      :fiddle/type :entity,
      :fiddle/pull
      "; synchronized with hyperfiddle.fiddle/browser-pull\n[:db/id\n :fiddle/css\n :fiddle/ident\n {:fiddle/links [:db/id\n                 :link/class\n                 {:link/fiddle [:db/id\n                                :fiddle/ident               ; routing\n                                :fiddle/query               ; validation\n                                :fiddle/type                ; validation\n                                ]}\n                 :link/formula\n                 :link/path\n                 :link/rel\n                 :link/tx-fn]}\n :fiddle/markdown\n :fiddle/pull\n :fiddle/pull-database\n :fiddle/query\n :fiddle/cljs-ns\n :fiddle/renderer\n :fiddle/type\n :fiddle/hydrate-result-as-fiddle\n *                                                          ; For hyperblog, so we can access :hyperblog.post/title etc from the fiddle renderer\n ]",
      :fiddle/css
      "table.hyperfiddle.-fiddle-links { table-layout: fixed; }\ntable.-fiddle-links th.-hypercrud-browser-path--fiddle-links { width: 60px; }\n\ntable.-fiddle-links td.-hypercrud-browser-path--fiddle-links--link-fiddle { display: flex; }\ntable.hyperfiddle.-fiddle-links td.field.-link-fiddle > select { flex: 0 1 80% !important; } /* line up :new */\n",
      :fiddle/ident :hyperfiddle/ide,
      :hyperfiddle/starred true}

     {:db/id 17592186061847,
      :fiddle/renderer "hyperfiddle.ide.fiddles.fiddle-src/fiddle-src-renderer",
      :fiddle/links
      [{:db/id 17592186061848, :link/class [:hf/remove], :link/rel :hf/remove}
       {:db/id 17592186061849, :link/class [:hf/remove], :link/path ":fiddle/links", :link/rel :hf/remove}
       {:db/id 17592186061850,
        :link/class [:hf/iframe],
        :link/fiddle
        {:db/id 17592186045605,
         :fiddle/ident :hyperfiddle.ide/fiddle-options,
         :fiddle/query "[:find (pull ?link [:db/id :fiddle/ident])\n :where (or [?link :fiddle/ident] [?link :fiddle/type])]",
         :fiddle/type :query},
        :link/rel :hf/iframe}
       {:db/id 17592186061851,
        :link/class [:hf/new],
        :link/fiddle {:db/id 17592186056398, :fiddle/ident :hyperfiddle.ide/new-fiddle, :fiddle/type :entity},
        :link/path ":link/fiddle",
        :link/rel :hf/affix}
       {:db/id 17592186061852,
        :link/class [:hf/new],
        :link/fiddle {:db/id 17592186058175, :fiddle/ident :hyperfiddle.ide/new-link, :fiddle/type :entity},
        :link/path ":fiddle/links",
        :link/rel :hf/affix}],
      :fiddle/type :entity,
      :fiddle/pull
      "; synchronized with hyperfiddle.fiddle/browser-pull\n[:db/id\n :fiddle/css\n :fiddle/ident\n {:fiddle/links [:db/id\n                 :link/class\n                 {:link/fiddle [:db/id\n                                :fiddle/ident               ; routing\n                                :fiddle/query               ; validation\n                                :fiddle/type                ; validation\n                                ]}\n                 :link/formula\n                 :link/path\n                 :link/rel\n                 :link/tx-fn]}\n :fiddle/markdown\n :fiddle/pull\n :fiddle/pull-database\n :fiddle/query\n :fiddle/cljs-ns\n :fiddle/renderer\n :fiddle/type\n :fiddle/hydrate-result-as-fiddle\n *                                                          ; For hyperblog, so we can access :hyperblog.post/title etc from the fiddle renderer\n ]",
      :fiddle/css
      "table.hyperfiddle.-fiddle-links { table-layout: fixed; }\ntable.-fiddle-links th.-hypercrud-browser-path--fiddle-links { width: 60px; }\n\ntable.-fiddle-links td.-hypercrud-browser-path--fiddle-links--link-fiddle { display: flex; }\ntable.hyperfiddle.-fiddle-links td.field.-link-fiddle > select { flex: 0 1 80% !important; } /* line up :new */\n",
      :fiddle/ident :hyperfiddle/ide,
      :hyperfiddle/starred true}
     ]
    }
   )


(def schema
  (contrib.datomic/indexed-schema
     '({:db/ident :attribute/ident, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "FK to schema, they can't be directly on $ schema because attribute renderers are a \"source code\" concern. TODO: move these off domain and into the fiddle repo."}
         {:db/ident :attribute/renderer, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Default attribute renderer, a CLJS var like `hyperfiddle.ui.controls/code`."}
         {:db/ident :community/category, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/many}, :db/fulltext true, :db/doc "All community categories"}
         {:db/ident :community/name, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/fulltext true, :db/doc "A community's name"}
         {:db/ident :community/neighborhood, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "A community's neighborhood"}
         {:db/ident :community/orgtype, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "A community orgtype enum value"}
         {:db/ident :community/type, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "Community type enum values"}
         {:db/ident :community/url, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "A community's url"}
         {:db/ident :db/cardinality, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of an attribute. Two possible values: :db.cardinality/one for single-valued attributes, and :db.cardinality/many for many-valued attributes. Defaults to :db.cardinality/one."}
         {:db/ident :db/code, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/fulltext true, :db/doc "String-valued attribute of a data function that contains the function's source code."}
         {:db/ident :db/doc, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/fulltext true, :db/doc "Documentation string for an entity."}
         {:db/ident :db/excise, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :db/fn, :db/valueType {:db/ident :db.type/fn}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "A function-valued attribute for direct use by transactions and queries."}
         {:db/ident :db/fulltext, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of an attribute. If true, create a fulltext search index for the attribute. Defaults to false."}
         {:db/ident :db/ident, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "Attribute used to uniquely name an entity."}
         {:db/ident :db/index, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of an attribute. If true, create an AVET index for the attribute. Defaults to false."}
         {:db/ident :db/isComponent, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of attribute whose vtype is :db.type/ref. If true, then the attribute is a component of the entity referencing it. When you query for an entire entity, components are fetched automatically. Defaults to nil."}
         {:db/ident :db/lang, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Attribute of a data function. Value is a keyword naming the implementation language of the function. Legal values are :db.lang/java and :db.lang/clojure"}
         {:db/ident :db/noHistory, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of an attribute. If true, past values of the attribute are not retained after indexing. Defaults to false."}
         {:db/ident :db/txInstant, :db/valueType {:db/ident :db.type/instant}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Attribute whose value is a :db.type/instant. A :db/txInstant is recorded automatically with every transaction."}
         {:db/ident :db/unique, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of an attribute. If value is :db.unique/value, then attribute value is unique to each entity. Attempts to insert a duplicate value for a temporary entity id will fail. If value is :db.unique/identity, then attribute value is unique, and upsert is enabled. Attempting to insert a duplicate value for a temporary entity id will cause all attributes associated with that temporary id to be merged with the entity already in the database. Defaults to nil."}
         {:db/ident :db/valueType, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Property of an attribute that specifies the attribute's value type. Built-in value types include, :db.type/keyword, :db.type/string, :db.type/ref, :db.type/instant, :db.type/long, :db.type/bigdec, :db.type/boolean, :db.type/float, :db.type/uuid, :db.type/double, :db.type/bigint,  :db.type/uri."}
         {:db/ident :db.alter/attribute, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will alter the definition of existing attribute v."}
         {:db/ident :db.excise/attrs, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}}
         {:db/ident :db.excise/before, :db/valueType {:db/ident :db.type/instant}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :db.excise/beforeT, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :db.install/attribute, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as an attribute."}
         {:db/ident :db.install/function, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as a data function."}
         {:db/ident :db.install/partition, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as a partition."}
         {:db/ident :db.install/valueType, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as a value type."}
         {:db/ident :db.sys/partiallyIndexed, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "System-assigned attribute set to true for transactions not fully incorporated into the index"}
         {:db/ident :db.sys/reId, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "System-assigned attribute for an id e in the log that has been changed to id v in the index"}
         {:db/ident :didier/team-name, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :district/name, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "A unique district name (upsertable)"}
         {:db/ident :district/region, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "A district region enum value"}
         {:db/ident :dustingetz/foo, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz/foo1, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz/next, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz/prev, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.gist/src-clojure, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.nav/children, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "Each nav item has optional children"}
         {:db/ident :dustingetz.post/draft-placeholder, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Show in rendered view, but disabled (no link)"}
         {:db/ident :dustingetz.post/hidden, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Omit from user-facing view but show in admin dashboards"}
         {:db/ident :dustingetz.post/published, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.post/published-date, :db/valueType {:db/ident :db.type/instant}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.post/related, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "This post links to these posts, e.g. a table of contents, or \"if you liked this you might also like\""}
         {:db/ident :dustingetz.post/slug, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "Human readable unique identifier for this post "}
         {:db/ident :dustingetz.post/sort-index, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.post/title, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.reg/age, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.reg/birthdate, :db/valueType {:db/ident :db.type/instant}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.reg/email, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}}
         {:db/ident :dustingetz.reg/gender, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.reg/name, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.reg/shirt-size, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.slack-converter/raw-source, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.storm/channel, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.storm/ident, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}}
         {:db/ident :dustingetz.streaker/comment, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/date, :db/valueType {:db/ident :db.type/instant}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/diet, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/eating, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/exercise, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/many}}
         {:db/ident :dustingetz.streaker/food-log, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/gym, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Generally PIT"}
         {:db/ident :dustingetz.streaker/intention, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/miles-ran, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Miles run, should be float."}
         {:db/ident :dustingetz.streaker/mood, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/pt, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "personal trainer"}
         {:db/ident :dustingetz.streaker/teatotal, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/weight, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Should be decimal, but js/jvm coercion issues"}
         {:db/ident :dustingetz.streaker/weight-6am, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker/yoga, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.streaker.lifting/bench-1rm, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "One rep max"}
         {:db/ident :dustingetz.streaker.lifting/squat-1rm, :db/valueType {:db/ident :db.type/long}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "One rep max"}
         {:db/ident :dustingetz.task/completed, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.task/parent, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :dustingetz.task/title, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :fiddle/cljs-ns, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "ClojureScript `user` namespace, available in :fiddle/renderer."}
         {:db/ident :fiddle/css, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Fiddle CSS. \n\nWarning: CSS is not scoped, please write targetted CSS"}
         {:db/ident :fiddle/hydrate-result-as-fiddle, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Experimental. When set, data-sync will interpret this fiddle's result as a fiddle - like a higher order fiddle - this is a recursion mechanic."}
         {:db/ident :fiddle/ident, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "Fiddle identifier used in URLs. Warning: changing this breaks fiddle URLs."}
         {:db/ident :fiddle/links, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}, :db/isComponent true, :db/doc "Links to other fiddles that are available from this fiddle"}
         {:db/ident :fiddle/markdown, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Markdown expression for fiddle view, optional"}
         {:db/ident :fiddle/pull, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Datomic pull expression for the entity addressed by the URL"}
         {:db/ident :fiddle/pull-database, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Argument to `datomic.api/pull`, defaults to $"}
         {:db/ident :fiddle/query, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Datomic query datalog. \n\nWarning: no support yet for rules, d/history, d/log or other datomic API access."}
         {:db/ident :fiddle/renderer, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Reagent expression for the fiddle view"}
         {:db/ident :fiddle/type, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Which Datomic query API"}
         {:db/ident :fiddle/uuid, :db/valueType {:db/ident :db.type/uuid}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "For naming anonymous fiddles"}
         {:db/ident :fressian/tag, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "Keyword-valued attribute of a value type that specifies the underlying fressian type used for serialization."}
         {:db/ident :helloworld/email, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "hello world email "}
         {:db/ident :hyperfiddle/archived, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :hyperfiddle/deprecated, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :hyperfiddle/owners, :db/valueType {:db/ident :db.type/uuid}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "Entity owners uuids, used by ACLs"}
         {:db/ident :hyperfiddle/starred, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :link/class, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/many}, :db/doc "semantic selector, like html css classes"}
         {:db/ident :link/fiddle, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "link target"}
         {:db/ident :link/formula, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "deprecated; function hook to influence target query inputs wiring, this is fully managed now"}
         {:db/ident :link/path, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "todo rename; specifies the attribute for which this link is valid"}
         {:db/ident :link/rel, :db/valueType {:db/ident :db.type/keyword}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "archived"}
         {:db/ident :link/tx-fn, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "names a hyperfiddle.api/tx-fn multimethod which builds a transaction"}
         {:db/ident :neighborhood/district, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/one}, :db/doc "A neighborhood's district"}
         {:db/ident :neighborhood/name, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}, :db/doc "A unique neighborhood name (upsertable)"}
         {:db/ident :person/name, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}, :db/unique {:db/ident :db.unique/identity}}
         {:db/ident :person/parents, :db/valueType {:db/ident :db.type/ref}, :db/cardinality {:db/ident :db.cardinality/many}}
         {:db/ident :putterson.todone/done, :db/valueType {:db/ident :db.type/boolean}, :db/cardinality {:db/ident :db.cardinality/one}}
         {:db/ident :putterson.todone/title, :db/valueType {:db/ident :db.type/string}, :db/cardinality {:db/ident :db.cardinality/one}})
     ))

(def schemas {"$" schema})