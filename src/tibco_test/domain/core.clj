(ns tibco-test.domain.core
    (:require [clojure.string :as clj-str]
              [ladybird.domain.core :refer (def-typed-domain)]
              [ladybird.data.cond :refer (raw)]
              [ladybird.util.symbol :as sym]
              [ladybird.util.keyword :as kw]
              [ladybird.util.string :as str]))

;; typed domain
(def query-total-from-fn-doc-string-template
     "query %s by condition and order, returns results from specified offset and up to limit rows, with the total count of results.
      Params:
          query-spec    --    query specification
          condition     --    query condition
          need-total    --    If it's logically true, the function will get the total count of query result from database and return it. Otherwise, -1 will be returned as :total value.
          order         --    query order
          offset        --    query offset
          limit         --    query limit
      Returns:
          a map         --    {:total <total count of results> :data <seq of result records>}

      e.g.
          (query-total-member-from (make (= :age 20)) true [:name] 10 20)  ; need-total is true, so the total count of query result will be retrieved from database
              =>  {:total 101 :data ({:name \"a\" :age 20} {:name \"b\" :age 20} ...)}
          (query-total-member-from (make (= :age 20)) false [:name] 10 20) ; need-total is false, so -1 will be returned as total value.
              =>  {:total -1 :data ({:name \"a\" :age 20} {:name \"b\" :age 20} ...)}
          (query-total-member-from (make (= :age 200)) false [:name] 10 20)  ; if query result is empty, the value of :data slot in the returned map will be ()
              => {:total -1 :data ()}
      "
     )

(defmacro defdomain [domain & args]
  (let [clj-name (-> domain name str/clj-case)
        query-total-from-fn-name (str "query-total-" clj-name "-from")
        query-total-from-fn-doc-string (format query-total-from-fn-doc-string-template clj-name)
        query-total-from-fn-sym (symbol query-total-from-fn-name)
        query-from-fn-sym (-> (str "query-" clj-name "-from") symbol)
        ]
    `(do
       (def-typed-domain ~domain ~@args)
       (def ~domain (assoc ~domain :query-total-from-fn-meta [~query-total-from-fn-name ~query-total-from-fn-doc-string]))
       (defn ~query-total-from-fn-sym
         ([~'condition ~'need-total ~'order ~'offset ~'limit]
           (~query-total-from-fn-sym ~domain ~'condition ~'need-total ~'order ~'offset ~'limit))
         ([~'query-spec ~'condition ~'need-total ~'order ~'offset ~'limit]
           (if ~'need-total
             (let [spec# (update-in ~'query-spec [:fields] conj [(raw "count(*) over()") :--total])
                   records# (~query-from-fn-sym spec# ~'condition ~'order ~'offset ~'limit)
                   total# (-> records# first (:--total 0))
                   ]
               {:total total# :data (map #(dissoc % :--total) records# )})
             {:total -1 :data (~query-from-fn-sym ~'query-spec ~'condition ~'order ~'offset ~'limit)}))))))



