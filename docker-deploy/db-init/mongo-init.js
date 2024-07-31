print("#### MONGO-INIT.JS ####");

print("Creating new database: marine_unit_monitoring.");
db = new Mongo().getDB("marine_unit_monitoring");


// USERS COLLECTION
print("-- Executing init operations for: users collection. --");

print("Creating users collection.");
db.createCollection('users');

print("Inserting init-temp document.");
db.users.insertOne({temp: "temp"});

print("Creating unique index for email field.");
db.users.createIndex({email: 1}, {unique: true})

print("Deleting init-temp document.");
db.users.deleteOne({temp: "temp"});


// SHIP_TRACKS COLLECTION
print("-- Executing init operations for: ship_tracks collection. --");

print("Creating ship_tracks collection.");
db.createCollection('ship_tracks');


// VERIFICATION_TOKEN COLLECTION
print("-- Executing init operations for: verification_tokens collection. --");

print("Creating verification_tokens collection.");
db.createCollection('verification_tokens');


// JWT_TOKENS COLLECTION
print("-- Executing init operations for: jwt_tokens collection. --");

print("Creating jwt_tokens collection.");
db.createCollection('jwt_tokens');


// ACTIVE_POINTS COLLECTION
print("-- Executing init operations for: active_points collection. --");

print("Creating active_points collection.");
db.createCollection('active_points');