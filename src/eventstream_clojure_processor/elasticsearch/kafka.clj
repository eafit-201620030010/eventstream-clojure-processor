(ns eventstream-clojure-processor.elasticsearch.kafka
  (:require
   [cheshire.core :as json]
   [clojure.core.async :as async]
   [clojure.java.io :as jio])
  (:import
   (java.time Duration)
   (java.util Properties)
   (org.apache.kafka.clients.consumer ConsumerConfig KafkaConsumer)))

(defn- build-properties [config-fname]
  (with-open [config (jio/reader config-fname)]
    (doto (Properties.)
      (.putAll {ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:9092"
                ConsumerConfig/GROUP_ID_CONFIG        "wikimedia-processing-group"
                ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG   "org.apache.kafka.common.serialization.StringDeserializer"
                ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG "org.apache.kafka.common.serialization.StringDeserializer"})
      (.load config))))

(defn consume-wikimedia-events [config-fname topic process-fn]
  (with-open [consumer (KafkaConsumer. (build-properties config-fname))]
    (.subscribe consumer [topic])
    (loop [processed-events 0]
      (try
        (let [records (.poll consumer (Duration/ofMillis 100))]
          (doseq [record records]
            (let [event-data (.value record)]
              (process-fn event-data))
            (inc processed-events)))
        (catch Exception e
          (println (str "Error when consuming Kafka events:" (.getMessage e)))))
      (recur processed-events))))
