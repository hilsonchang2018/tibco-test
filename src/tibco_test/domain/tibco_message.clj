(ns tibco-test.domain.tibco-message
  (:require [tibco-test.domain.core :refer (defdomain)]
            [ladybird.data.enum :refer (defenum)]
            [ladybird.data.build-in-validator :refer (not-nil)]
            ))

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

(defdomain TibcoMessage
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


