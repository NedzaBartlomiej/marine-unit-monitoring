db = db.getSiblingDB('marine_unit_monitoring');

db.createCollection('ship_track_history');
db.createCollection('users');
db.createCollection('active_points');
db.createCollection('jwt_blacklist');
db.createCollection('email_verifications');

db.users.createIndex({email: 1}, {unique: true}) // todo not working (i needed to set this by myself in mongosh)