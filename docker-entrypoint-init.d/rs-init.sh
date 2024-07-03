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

#todo automate it - maybe i should add the config file and it will be work and reconfig
# add it to readme in installation info
#cfg = rs.conf()
#cfg.members[0].priority = 100
#cfg.members[1].priority = 1
#cfg.members[2].priority = 1
#rs.reconfig(cfg)