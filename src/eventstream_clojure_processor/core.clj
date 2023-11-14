(ns eventstream-clojure-processor.core
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as jio]
   [clj-http.client :as client])
  (:import
   (java.util Properties)
   (org.apache.kafka.clients.admin AdminClient NewTopic)
   (org.apache.kafka.clients.producer Callback KafkaProducer ProducerConfig ProducerRecord)
   (org.apache.kafka.common.errors TopicExistsException)))

(defn- build-properties [config-fname]
  (with-open [config (jio/reader config-fname)]
    (doto (Properties.)
      (.putAll
       {ProducerConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:9092"
        ProducerConfig/KEY_SERIALIZER_CLASS_CONFIG   "org.apache.kafka.common.serialization.StringSerializer"
        ProducerConfig/VALUE_SERIALIZER_CLASS_CONFIG "org.apache.kafka.common.serialization.StringSerializer"})
      (.load config))))

(defn- create-topic! [topic partitions replication cloud-config]
  (let [ac (AdminClient/create cloud-config)]
    (try
      (.createTopics ac [(NewTopic. ^String topic (int partitions) (short replication))])
      (catch TopicExistsException e nil)
      (finally
        (.close ac)))))

(defn process-data [data producer topic]
  (let [key "wikimedia-data"
        value data]
    (.send producer (ProducerRecord. topic key value))
    (println "Data sent to Kafka topic.")))

(defn handle-events [config-fname topic]
  (println "Initiating event handling...")
  (let [url "https://stream.wikimedia.org/v2/stream/recentchange"
        start-time (System/currentTimeMillis)
        processing-time (* 1 60 1000)
        props (build-properties config-fname)
        kafka-producer (KafkaProducer. props)]
    (create-topic! topic 1 3 props)
    (try
      (with-open [reader (java.io.BufferedReader. (java.io.InputStreamReader. (-> (client/get url {:as :stream}) :body)))]
        (loop [line (.readLine reader)
               elapsed-time (- (System/currentTimeMillis) start-time)]
          (when (and line (<= elapsed-time processing-time))
            (when (.startsWith line "data:")
              (process-data line kafka-producer topic))
            (recur (.readLine reader) (- (System/currentTimeMillis) start-time))))
        (println "End of event handling."))
      (catch Exception e
        (println "Connection error:" e)))
    (.close kafka-producer)))

(defn -main [& args]
  (if (count args) < 2)
  (println "Please provide the path to the Kafka config file and the Kafka topic.")
  (let [config-file (first args)
        topic (second args)]
    (handle-events config-file topic)))
