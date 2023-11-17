(defproject eventstream-clojure-processor "0.1.0-SNAPSHOT"
  :description "Procesamiento de eventos con Clojure"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [cheshire "5.10.0"]
                 [org.apache.kafka/kafka-clients "2.8.0"]
                 [clj-http "3.12.0"]
                 [cc.qbits/spandex "0.8.2"]]
  :aliases {"wikimedia-stream" ["run" "-m" "eventstream-clojure-processor.eventstreams.main"]
            "consumer-kafka-elastic" ["run" "-m" "eventstream-clojure-processor.elasticsearch.elasticsearch-main"]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
