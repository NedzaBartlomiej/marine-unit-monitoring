#!/bin/sh
echo "#### RS.INIT.SH ####"

mongosh <<BLOCK
print("-- Initializing replica set. --");
rs.initiate({
    "_id": "marine-unit-monitoring-db-rs",
    "members": [
        {
          "_id": 0,
          "host": "mongodb-primary:27017"
        },
        {
          "_id": 1,
          "host": "mongodb2:27017"
        },
        {
          "_id": 2,
          "host": "mongodb3:27017"
        }
    ]
});

print("-- Reconfiguring to set mongodb-primary as PRIMARY member. --");
cfg = rs.conf();
cfg.members[0].priority = 2;
cfg.members[1].priority = 0;
cfg.members[2].priority = 0;
rs.reconfig(cfg, {force: true});
BLOCK