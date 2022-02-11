FROM debezium/connect:1.8
ENV KAFKA_CONNECT_JDBC_DIR=$KAFKA_CONNECT_PLUGINS_DIR/kafka-connect-jdbc \
    KAFKA_CONNECT_ES_DIR=$KAFKA_CONNECT_PLUGINS_DIR/kafka-connect-elasticsearch \
    KAFKA_CONNECT_JSON_DIR=$KAFKA_CONNECT_PLUGINS_DIR/kafka-connect-json

ARG POSTGRES_VERSION=42.3.1
ARG KAFKA_JDBC_VERSION=10.3.1
ARG KAFKA_JSON_VERSION=6.0.5

# Deploy PostgreSQL JDBC Driver
RUN cd /kafka/libs && curl -sO https://jdbc.postgresql.org/download/postgresql-$POSTGRES_VERSION.jar

# Deploy Kafka Connect JDBC
RUN mkdir $KAFKA_CONNECT_JDBC_DIR && cd $KAFKA_CONNECT_JDBC_DIR &&\
	curl -sO https://packages.confluent.io/maven/io/confluent/kafka-connect-jdbc/$KAFKA_JDBC_VERSION/kafka-connect-jdbc-$KAFKA_JDBC_VERSION.jar

# Deploy Kafka Connect JSON Schema Converter
RUN mkdir $KAFKA_CONNECT_JSON_DIR && cd $KAFKA_CONNECT_JSON_DIR &&\
	curl -sO https://packages.confluent.io/maven/io/confluent/kafka-connect-json-schema-converter/$KAFKA_JSON_VERSION/kafka-connect-json-schema-converter-$KAFKA_JSON_VERSION.jar &&\
    curl -sO https://packages.confluent.io/maven/io/confluent/kafka-json-schema-provider/$KAFKA_JSON_VERSION/kafka-json-schema-provider-$KAFKA_JSON_VERSION.jar &&\
    curl -sO https://packages.confluent.io/maven/io/confluent/kafka-json-schema-serializer/$KAFKA_JSON_VERSION/kafka-json-schema-serializer-$KAFKA_JSON_VERSION.jar &&\
    curl -sO https://packages.confluent.io/maven/io/confluent/kafka-schema-serializer/$KAFKA_JSON_VERSION/kafka-schema-serializer-$KAFKA_JSON_VERSION.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-guava/2.12.3/jackson-datatype-guava-2.12.3.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.12.3/jackson-datatype-jdk8-2.12.3.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.12.3/jackson-datatype-jsr310-2.12.3.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-joda/2.12.3/jackson-datatype-joda-2.12.3.jar &&\
    curl -sO https://repo1.maven.org/maven2/joda-time/joda-time/2.10.8/joda-time-2.10.8.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/fasterxml/jackson/module/jackson-module-parameter-names/2.12.3/jackson-module-parameter-names-2.12.3.jar &&\
    curl -sO https://packages.confluent.io/maven/io/confluent/common-config/$KAFKA_JSON_VERSION/common-config-$KAFKA_JSON_VERSION.jar &&\
    curl -sO https://repo1.maven.org/maven2/org/json/json/20211205/json-20211205.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/google/guava/guava/31.0.1-jre/guava-31.0.1-jre.jar &&\
    curl -sO https://repo1.maven.org/maven2/com/damnhandy/handy-uri-templates/2.1.8/handy-uri-templates-2.1.8.jar &&\
    curl -sO https://repository.mulesoft.org/nexus/content/repositories/public/com/github/everit-org/json-schema/org.everit.json.schema/1.12.2/org.everit.json.schema-1.12.2.jar