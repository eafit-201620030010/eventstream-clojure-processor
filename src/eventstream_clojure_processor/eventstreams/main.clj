(ns eventstream-clojure-processor.eventstreams.main
  (:require [eventstream-clojure-processor.eventstreams.kafka-utils :as kafka-utils]
            [eventstream-clojure-processor.eventstreams.eventstream-handler :as eventstream-handler]))

(defn -main [& args]
  (if (not= (count args) 2)
    (println "Please provide the path to the Kafka config file and the Kafka topic.")
    (let [config-file (first args)
          topic (second args)]
      (eventstream-handler/handle-event-stream config-file topic))))
