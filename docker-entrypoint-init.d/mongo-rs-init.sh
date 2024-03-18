#!/bin/bash

echo "########### Waiting for replica 01 ###########"
until mongo --host mongodb1 --eval "printjson(db.runCommand({ serverStatus: 1}).ok)"
  do
    echo "########### Sleeping  ###########"
    sleep 5
  done


echo "########### Waiting for replica 02  ###########"
until mongo --host mongodb2 --eval "printjson(db.runCommand({ serverStatus: 1}).ok)"
  do
    echo "########### Sleeping  ###########"
    sleep 5
  done


echo "########### Waiting for replica 03  ###########"
until mongo --host mongodb3 --eval "printjson(db.runCommand({ serverStatus: 1}).ok)"
  do
    echo "########### Sleeping  ###########"
    sleep 5
  done

echo "#### INITIALIZING MONGODB REPLICA SET ####"
mongosh --host mongodb1 <<EOF
rs.initiate({
    _id: 'marine-unit-monitoring-db-rs',
    version: 1,
    members: [
        {_id: 0, host: 'mongodb1:27017'},
        {_id: 1, host: 'mongodb2:27017'},
        {_id: 2, host: 'mongodb3:27017'}
    ]
})
EOF