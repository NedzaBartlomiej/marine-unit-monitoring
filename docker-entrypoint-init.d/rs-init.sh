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

# add it to readme in installation info
#rs.conf().members[0].priority = 100
#rs.conf().members[1].priority = 1
#rs.conf().members[2].priority = 1
#rs.reconfig(rs.conf())