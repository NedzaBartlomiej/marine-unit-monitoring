#!/bin/bash
echo "---- 'start.sh' ----"

echo "#### BUILDING DOCKER ####"
docker-compose up -d

echo "#### WAITING FOR ALL CONTAINERS START ####"
sleep 10

echo "#### EXECUTING INIT FILES ####"
docker exec mongodb-primary usr/docker-entrypoint-init.d/rs-init.sh