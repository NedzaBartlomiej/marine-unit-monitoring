#!/bin/bash

echo "RS.INITIATE()"
mongosh <<EOF
rs.initiate({
    _id: 'marine-unit-monitoring-db-rs',
    version: 1,
    members: [
        {_id: 0, host: 'mongodb-primary:27017'},
        {_id: 1, host: 'mongodb2:27017'},
        {_id: 2, host: 'mongodb3:27017'}
    ]
})
EOF