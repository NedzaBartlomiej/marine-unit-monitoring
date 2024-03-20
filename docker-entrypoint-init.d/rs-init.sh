#!/bin/bash
echo "---- es-init.sh ----"

echo "#### RS.INITIATE() ####"
mongosh <<BLOCK
rs.initiate({
    _id: 'marine-unit-monitoring-db-rs',
    version: 1,
    members: [
        {_id: 0, host: 'mongodb-primary:27017'},
        {_id: 1, host: 'mongodb2:27017'},
        {_id: 2, host: 'mongodb3:27017'}
    ]
})
BLOCK