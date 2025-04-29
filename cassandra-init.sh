#!/bin/bash

docker exec -it cassandra bash -c "cqlsh -u cassandra -p cassandra -e \"CREATE KEYSPACE digestwave WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};\""
