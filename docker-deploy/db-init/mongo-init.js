db = db.getSiblingDB('marine_unit_monitoring');

db.createCollection('ship_tracks');
db.createCollection('users');
db.createCollection('active_points');
db.createCollection('jwt_tokens');
db.createCollection('verification_tokens');

db.users.createIndex({email: 1}, {unique: true}) // todo not working (i needed to set this by myself in mongosh)