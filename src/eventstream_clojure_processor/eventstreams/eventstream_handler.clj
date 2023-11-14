(ns eventstream-clojure-processor.eventstreams.eventstream-handler
  (:require
   [eventstream-clojure-processor.eventstreams.kafka-utils :as kafka-utils]
   [clojure.data.json :as json]
   [clojure.java.io :as jio]
   [clj-http.client :as client])
  (:import
   (java.util Properties)
   (org.apache.kafka.clients.admin AdminClient NewTopic)
   (org.apache.kafka.clients.producer Callback KafkaProducer ProducerConfig ProducerRecord)
   (org.apache.kafka.common.errors TopicExistsException)))

(defn- process-data [data producer topic]
  (let [key "wikimedia-data"
        value data]
    (.send producer (ProducerRecord. topic key value))
    (println "Data sent to Kafka topic.")))

(defn handle-events [reader kafka-producer topic]
  (loop [line (.readLine reader)]
    (when line
      (when (.startsWith line "data:")
        (process-data line kafka-producer topic))
      (recur (.readLine reader)))))

(defn- create-topic! [topic partitions replication cloud-config]
  (let [ac (AdminClient/create cloud-config)]
    (try
      (.createTopics ac [(NewTopic. ^String topic (int partitions) (short replication))])
      (catch TopicExistsException e nil)
      (finally
        (.close ac)))))

(defn handle-event-stream [config-fname topic]
  (println "Initiating event handling...")
  (let [url "https://stream.wikimedia.org/v2/stream/recentchange"
        start-time (System/currentTimeMillis)
        processing-time (* 1 60 1000)
        props (kafka-utils/build-properties config-fname)
        kafka-producer (KafkaProducer. props)]
    (kafka-utils/create-topic! topic 1 3 props)
    (try
      (with-open [reader (java.io.BufferedReader. (java.io.InputStreamReader. (-> (client/get url {:as :stream}) :body)))]
        (loop [elapsed-time (- (System/currentTimeMillis) start-time)]
          (when (<= elapsed-time processing-time)
            (handle-events reader kafka-producer topic)
            (recur (- (System/currentTimeMillis) start-time))))
        (println "End of event handling."))
      (catch Exception e
        (println "Connection error:" e)))
    (.close kafka-producer)))
