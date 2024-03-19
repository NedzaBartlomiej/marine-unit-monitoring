#!/bin/bash

docker-compose up -d

sleep 5

docker exec mongodb-primary usr/docker-entrypoint-init.d/rs-init.sh