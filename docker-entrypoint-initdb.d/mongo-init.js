db = db.getSiblingDB('marine_unit_monitoring');

db.createCollection('ship_track_history');
db.createCollection('tracked_ships');
db.createCollection('users');