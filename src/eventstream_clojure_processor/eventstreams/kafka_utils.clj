(ns eventstream-clojure-processor.eventstreams.kafka-utils
  (:require
   [clojure.java.io :as jio])
  (:import
   (java.util Properties)
   (org.apache.kafka.clients.admin AdminClient NewTopic)
   (org.apache.kafka.clients.producer ProducerConfig)
   (org.apache.kafka.common.errors TopicExistsException)))


(defn build-properties [config-fname]
  (with-open [config (jio/reader config-fname)]
    (doto (Properties.)
      (.putAll
       {ProducerConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:9092"
        ProducerConfig/KEY_SERIALIZER_CLASS_CONFIG   "org.apache.kafka.common.serialization.StringSerializer"
        ProducerConfig/VALUE_SERIALIZER_CLASS_CONFIG "org.apache.kafka.common.serialization.StringSerializer"})
      (.load config))))

(defn create-topic! [topic partitions replication cloud-config]
  (let [ac (AdminClient/create cloud-config)]
    (try
      (.createTopics ac [(NewTopic. ^String topic (int partitions) (short replication))])
      (catch TopicExistsException e nil)
      (finally
        (.close ac)))))
