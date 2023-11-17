(ns eventstream-clojure-processor.elasticsearch.elasticsearch-main
  (:require
   [qbits.spandex :as spandex]
   [clojure.data.json :as json-kafka]
   [clojure.java.io :as jio]
   [eventstream-clojure-processor.elasticsearch.kafka :as kafka]))

(defn create-elasticsearch-client [url]
  (let [options {:hosts [url]
                 :http-client {:basic-auth {:user "user" :password "password"}}}
        client (spandex/client options)]
    client))

(defn create-index [client index-name]
  (try
    (let [create-index-request {:index index-name}
          response (spandex/request client
                                    {:method :put
                                     :url [index-name]
                                     :body {:settings {"number_of_shards" 1
                                                       "number_of_replicas" 0}}})]
      (when (= (:status response) 200)
        (println (str "The index '" index-name "' has been created."))))
    (catch Exception e
      (let [status (-> e :data :status)]
        (println (str "Error creating index '" index-name "': " (.getMessage e)))
        (println (str "Response status: " status))))))

(defn send-messages [client index-name messages]
  (try
    (doseq [document messages]
      (spandex/request client
                       {:method :post
                        :url [index-name "_doc"]
                        :body document}))
    (println "Messages were sent to the index.")
    (catch Exception e
      (println (str "Error sending messages to Elasticsearch: " (.getMessage e))))))

(defn process-wikimedia-event [event client index-name]
  (try
    (let [json-part (subs event 6)
          parsed-data (json-kafka/read-str json-part {:key-fn keyword})]
      (when parsed-data
        (let [title (:title parsed-data)]
          (when title
            (println "Received message - Title: " title)
            (send-messages client index-name [parsed-data])))))
    (catch Exception e
      (println (str "Error processing Wikimedia event: " (.getMessage e))))))

(defn -main [& args]
  (try
    (let [elasticsearch-client (create-elasticsearch-client "https://eventstream-kafka-cl-5439517819.us-east-1.bonsaisearch.net:443")
          index-name "eventstream-wikimedia"
          kafka-config-fname (first args)
          kafka-topic (second args)]
      (kafka/consume-wikimedia-events kafka-config-fname kafka-topic #(process-wikimedia-event % elasticsearch-client index-name))
      (create-index elasticsearch-client index-name))
    (catch Exception e
      (println (str "Error in main function:" (.getMessage e))))))
