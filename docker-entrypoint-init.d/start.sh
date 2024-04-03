#!/bin/bash

app_img_name="marine-unit-monitoring"
app_container_name="marine-unit-monitoring"
primary_rs_instance="mongodb-primary"

echo "---- 'start.sh' ----"

echo "#### CLEANING ####"
echo "-- REMOVING NOT USED VOLUMES --"
docker volume prune
echo "-- REMOVING NOT USED IMAGES --"
docker image prune


echo "#### MVN CLEAN & PACKAGE ####"
if ! mvn clean; then
  echo "Something go wrong on mvn clean, exiting."
  exit 1;
fi
if ! mvn package; then
    echo "Something go wrong on mvn package, exiting."
    exit 1
fi


echo "#### STOPPING APP CONTAINERS ####"
echo "-- $app_container_name --"
docker stop $app_container_name
echo "-- mongodb-primary --"
docker stop mongodb-primary
echo "-- mongodb2 --"
docker stop mongodb2
echo "-- mongodb3 --"
docker stop mongodb3
echo "-- redis --"
docker stop redis


echo "#### UPDATING APP IMAGE -> '$app_img_name' ####"
echo "-- DELETING APP CONTAINER --"
docker rm $app_container_name
echo "-- DELETING APP IMAGE --"
docker rmi $app_img_name


echo "#### DOCKER COMPOSE ####"
docker-compose up -d


echo "#### CHECKING ARE CONTAINERS STARTED ####"
inst_status=$(docker container inspect $primary_rs_instance | jq -r '.[].State.Status')
until [ $inst_status = "running" ]; do
  echo $inst_status
  echo "Waiting for '$primary_rs_instance'"
  sleep 2
done
echo "-- Sleep 3 --"
sleep 3


echo "#### EXECUTING INIT FILES ####"
docker exec $primary_rs_instance usr/docker-entrypoint-init.d/rs-init.sh