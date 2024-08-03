#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "#### ${YELLOW}START.SH${NC} ####"

app_img_name="marine-unit-monitoring"
app_container_name="marine-unit-monitoring"
primary_rs_instance="mongodb-primary"
flag_file="./docker-deploy/script/start/initialized.flag"


echo -e "${YELLOW}PRUNING:${NC}"
echo "-- Removing not used volumes --"
docker volume prune -f
echo "-- Removing not used images --"
docker image prune -f


echo -e "${YELLOW}UPDATING APPLICATION TARGET:${NC}"
if ! mvn clean; then
  echo -e "${RED}Error: Something went wrong on mvn clean, exiting.${NC}"
  exit 1
fi

if ! mvn package; then
  echo -e "${RED}Error: Something went wrong on mvn package, exiting.${NC}"
  exit 1
fi


echo -e "${YELLOW}STOPPING APPLICATION CONTAINER:${NC}"
docker stop $app_container_name


echo -e "${YELLOW}UPDATING APPLICATION IMAGE: '$app_img_name' ####${NC}"
echo "-- Deleting app container --"
docker rm $app_container_name
echo "-- Deleting app image --"
docker rmi $app_img_name

echo -e "${YELLOW}DOCKER COMPOSE:${NC}"
docker-compose up -d



if [ -f "$flag_file" ]; then
  echo -e "${GREEN}Initialization already completed. Skipping execution.${NC}"
  exit 0
fi

check_container_running() {
  inst_status=$(docker container inspect $primary_rs_instance | jq -r '.[].State.Status')
  echo "$inst_status"
}

echo -e "${YELLOW}CHECKING IS MONGODB-PRIMARY CONTAINER RUNNING:${NC}"
inst_status=$(check_container_running)

until [ "$inst_status" = "running" ]; do
  echo "Current status: $inst_status"
  echo "Waiting for container '$primary_rs_instance' to be running..."
  sleep 2
  inst_status=$(check_container_running)
done

echo -e "${GREEN}Container '$primary_rs_instance' is now running.${NC}"



check_file_exists() {
  docker exec $primary_rs_instance test -e $1 && echo "exists" || echo "not_exists"
}

# RS-INIT.SH
echo -e "${YELLOW}CHECKING IS RS-INIT.SH FILE EXISTS IN THE MONGODB-PRIMARY CONTAINER:${NC}"
file_check="not_exists"
while [ "$file_check" = "not_exists" ]; do
  file_check=$(check_file_exists "db-init/rs-init.sh")
  if [ "$file_check" = "not_exists" ]; then
    echo "File rs-init.sh does not exist yet. Checking again in 5 seconds..."
    sleep 5
  fi
done

echo -e "${GREEN}File rs-init.sh exists. Executing docker exec...${NC}"
docker exec $primary_rs_instance db-init/rs-init.sh


# MONGO-INIT.JS
echo -e "${YELLOW}CHECKING IS MONGO-INIT.JS FILE EXISTS IN THE MONGODB-PRIMARY CONTAINER:${NC}"
file_check="not_exists"
while [ "$file_check" = "not_exists" ]; do
  file_check=$(check_file_exists "db-init/mongo-init.js")
  if [ "$file_check" = "not_exists" ]; then
    echo "File mongo-init.js does not exist yet. Checking again in 5 seconds..."
    sleep 5
  fi
done

echo -e "${GREEN}File mongo-init.js exists. Executing docker exec...${NC}"
docker exec $primary_rs_instance mongosh db-init/mongo-init.js


echo "Creating initialized.flag file."
touch "$flag_file"