(ns tibco-test.tibco-message
  (:require [clojure.string :as clj-str]
              [ladybird.domain.core :refer (def-typed-domain)]
              [ladybird.data.cond :refer (raw)]
              [ladybird.util.string :as str]
              [ladybird.data.enum :refer (defenum)]
              [ladybird.data.build-in-validator :refer (not-nil)]
              [ladybird.story.core :refer [defstory]]
              ))

(defmacro defdomain [domain & args]
  (let [clj-name (-> domain name str/clj-case)
        query-total-from-fn-name (str "query-total-" clj-name "-from")
        query-total-from-fn-doc-string (str "query-total-" clj-name "from-fn-doc-string-template")
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


(defenum TYPE
  :slot-add-rating "slot-add-rating"
  :slot-edit-rating "slot-edit-rating"
  :slot-void-rating "slot-void-rating"
  :table-open-rating "table-open-rating"
  :table-update-rating "table-update-rating"
  :table-close-rating "table-close-rating"
  :table-edit-rating "table-edit-rating"
  :table-void-rating "table-void-rating"
  :table-open-rating-reassign-from "table-open-rating-reassign-from"
  :table-open-rating-reassign-to "table-open-rating-reassign-to"
  :table-close-rating-reassign-from "table-close-rating-reassign-from"
  :table-close-rating-reassign-to "table-close-rating-reassign-to"
  )

(defdomain TibcoMessage_2018
  [:id             Long
   :type           _ TYPE         [not-nil enum:TYPE]
   :create-time ; this is UTC string format
   :jms-message-id
   :jms-destination
   :jms-delivery-time Long
   :jms-timestamp  Long
   :jms-content
   :unique-id
   :wynn-id
   :extracted-content
   ]
  {:db-maintain-fields [:id]
   }
  )

(defstory add-a-message [message]
  (add-tibco-message-2018! message))
