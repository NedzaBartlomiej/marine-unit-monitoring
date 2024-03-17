rs.initiate(
    {
        _id: "marine-unit-monitoring-db-rs",
        version: 1,
        members: [
            {_id: 0, host: "mongodb1:27017"},
            {_id: 1, host: "mongodb2:27017"},
            {_id: 2, host: "mongodb3:27017"}
        ]
    }
)


db = db.getSiblingDB('marine_unit_monitoring');

db.createCollection('ship_track_history');
db.createCollection('tracked_ships');
db.createCollection('users');