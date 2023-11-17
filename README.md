# eventstream-clojure-processor

## Prerequisites

- Java 8 or Java 11
- Clojure/Leiningen
- Kafka Cluster with Confluent Cloud
- Elasticsearch with Bonsai.io

```sh
Note: Please note that the Bonsai and Confluent Cloud accounts I utilize can be easily created for free, and the process takes no more than 5 minutes to complete
```

## Setup

```sh
git clone https://github.com/eafit-201620030010/eventstream-clojure-processor.git
```

- Configuration parameters to connect to your Kafka Cluster. for example: $HOME/.confluent/clojure.config

- Template configuration file for Confluent Cloud

```sh
# Required connection configs for Kafka producer, consumer, and admin
bootstrap.servers={{ BROKER_ENDPOINT }}
security.protocol=SASL_SSL
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username='{{ CLUSTER_API_KEY }}' password='{{ CLUSTER_API_SECRET }}';
sasl.mechanism=PLAIN
# Required for correctness in Apache Kafka clients prior to 2.6
client.dns.lookup=use_all_dns_ips

# Best practice for higher availability in Apache Kafka clients prior to 3.0
session.timeout.ms=45000

# Best practice for Kafka producer to prevent data loss
acks=all
```

- Config elasticsearch client {:user "" :password: ""} in elasticsearch_main.clj

```clojure
(defn create-elasticsearch-client [url]
  (let [options {:hosts [url]
                 :http-client {:basic-auth {:user "user" :password "password"}}}
        client (spandex/client options)]
    client))
```

- Config elasticsearch URL in elasticsearch_main.clj

```clojure
(create-elasticsearch-client "https://eventstream-kafka-cl-5439517819.us-east-1.bonsaisearch.net:443")
```

## Run

1. Wikimedia Event Stream

```sh
lein wikimedia-stream $HOME/.confluent/clojure.config wikimedia.recentchange
```

2. Consumer Kafka And Elasticsearch client

```sh
lein consumer-kafka-elastic $HOME/.confluent/clojure.config wikimedia.recentchange
```

## License

Copyright © 2023 jjchavarrg

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
