(ns tibco-test.core
  (:require  [ladybird.db.load :refer [load-db-file]]
             [clojure.string :as string]
             [clojure.tools.cli :refer [parse-opts]]
             [ladybird.util.core :refer (exception-str)]
             [taoensso.timbre :as log]
             [clojure.java.io :as io]
             [clj-time.core :as t]
             [clj-time.local :as l]
             [clj-time.format :as f]
             [cheshire.core :as cheshire]
             [redbull-transporter.story.tibco-message :refer (add-message)]
  (:gen-class))
  (:import (java.sql SQLException)))

(def cli-options
  ;; An option with a required argument
   [    
    ["-n" "--num 100" "the number of mock record"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 1000000000) "Must be a number between 0 and 1000000000"]]
   
   ["-f" "--first 1" "first rating id"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 1000000000) "Must be a number between 0 and 1000000000"]]
   
   ["-w" "--wynn 1" "first wynn-id"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 1000000000) "Must be a number between 0 and 1000000000"]]
   
   ["-c" "--wynncnt 1" "count of wynn-id"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 1000000000) "Must be a number between 0 and 1000000000"]]
   
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage: program-name [options]"
        ""
        "Options:"
        options-summary
        ""
        "--num xxx "
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      ; help => exit OK with usage summary
      (:help options) {:exit-message (usage summary) :ok? true}
      
      ; errors => exit with description of errors
      errors {:exit-message (error-msg errors)}
      
      ;; custom validation on arguments
      true {:options options}
      
      ; failed custom validation => exit with usage summary
      :else {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))



(def ^:private local-formatter (l/*local-formatters* :date-time-no-ms))

(defn now-str[]
(f/unparse local-formatter (t/now)))    
   

(defn write-db [uid wynn-id]
  (let [content {:coin-out 115.0
               :points-earned 19
               :coin-in 76.0
               :property "WP"
               :start-time "2017-07-10 18:05:49.0"
               :bank "00"
               :type "slot-add-rating"
               :end-time "2017-07-10 18:06:04.0"
               :wynn-id (str wynn-id)
               :location "01"
               :session "88"
               :win -39.0
               }
        whole-msg {:type "slot-add-rating"
                      :create-time (str (t/now))
                      :jms-message-id uid
                      :jms-destination ""
                      :jms-delivery-time 1496664334805
                      :jms-timestamp 1496664334805
                      :unique-id uid
                      :wynn-id (str wynn-id)
                      :extracted-content (cheshire/generate-string content)
                    }
        ]
  (add-message whole-msg)
  (println whole-msg)
))

(def ^:private shutdown-flag (atom false))

(defn set-shutdown! []
  (reset! shutdown-flag true))

(def ^:private period-seconds 30)

(defn destroy []
  (println "Shutting down...")
  (set-shutdown!)
  (Thread/sleep (* (+ period-seconds 3) 1000))
  (shutdown-agents)
  )

(defn add-shutdown-hook [callback-fn]
  (.addShutdownHook (Runtime/getRuntime) (Thread. callback-fn)))

(defn write-records
  [params]
  (let [{:keys [first num wynn wynncnt]} params]
    (println params)
    (loop [i first]
          (when (< i (+ first num))
            (write-db (str (now-str) "-" i) (+ (mod i wynncnt) wynn))
            (recur (inc i))))
  ))

(defn- time-to-gen [{:keys [interval options]}]
  (let [{:keys [first num]} options ]
  (loop [pos first]
   (when (not @shutdown-flag)
     (Thread/sleep (* interval 1000))
     (try
       (println (now-str))
       (write-records (assoc options :first pos))
      (catch Exception e
         (println e)
         ))
     (recur (+ pos num))))
     (println "gen record agent shutting down")
  ))

(defn start-task [interval-seconds options]
  (let [gen-record-agent (agent {:interval interval-seconds :options options})]
    (send-off gen-record-agent time-to-gen)
    ))

(defn -main  [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (load-db-file "tmp/db.def")
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (start-task  period-seconds options))))
  
