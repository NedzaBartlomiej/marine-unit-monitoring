db = db.getSiblingDB('marine_unit_monitoring');

db.createCollection('ship_track_history');
db.createCollection('users');
db.createCollection('active_points');

db.users.createIndex({email: 1}, {unique: true})