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

# todo run mvn package

# todo error handler - validation - bo przy starcie pierwszym zeby nie wyswietlalo natywnych bledow tylko moje opisy
echo "#### UPDATING APP IMAGE -> '$app_img_name' ####"
echo "-- BUILDING UPDATED IMAGE --"
docker build -t $app_img_name:latest .. # ^^ tylko w momencie gdy jest img:latest || img:1.0
echo "-- COMMITTING NEW VERSION OF IMAGE --"
docker commit $app_img_name $app_img_name:latest # ^^ tylko w momencie gdy


echo "#### DOCKER COMPOSE ####"
docker-compose up -d


echo "#### CLEANING ####"
echo "-- REMOVING NOT USED IMAGES --"
docker image prune


echo "#### WAITING FOR ALL CONTAINERS START ####" #todo: maybe somehow automate it
sleep 10


echo "#### EXECUTING INIT FILES ####"
docker exec mongodb-primary usr/docker-entrypoint-init.d/rs-init.sh