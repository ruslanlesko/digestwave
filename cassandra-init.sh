#!/bin/bash

SCRIPT=$(<scripts/init.cql)

docker exec -it cassandra bash -c "cqlsh -u cassandra -p cassandra -e \"$SCRIPT\""
