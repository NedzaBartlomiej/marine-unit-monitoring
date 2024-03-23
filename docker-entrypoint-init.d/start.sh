#!/bin/bash

app_img_name="marine-unit-monitoring"
app_container_name="marine-unit-monitoring"

echo "---- 'start.sh' ----"

echo "#### CLEANING ####"
echo "-- REMOVING NOT USED VOLUMES --"
docker volume prune
echo "-- REMOVING NOT USED IMAGES --"
docker image prune


echo "#### STOPPING APP CONTAINER ####"
docker stop $app_container_name


echo "#### MVN CLEAN & PACKAGE ####"
if ! mvn clean; then
  echo "Something go wrong on mvn clean, exiting."
  exit 1;
fi
if ! mvn package; then
    echo "Something go wrong on mvn package, exiting."
    exit 1
fi


echo "#### UPDATING APP IMAGE -> '$app_img_name' ####"
echo "-- DELETING APP CONTAINER --"
docker rm $app_container_name
echo "-- DELETING APP IMAGE --"
docker rmi $app_img_name


echo "#### DOCKER COMPOSE ####"
docker-compose up -d


echo "#### WAITING FOR ALL CONTAINERS START ####" #todo: maybe somehow automate it - sprawdzic czy mongodb-primary uruchomiony i tyle
sleep 10


echo "#### EXECUTING INIT FILES ####"
docker exec mongodb-primary usr/docker-entrypoint-init.d/rs-init.sh