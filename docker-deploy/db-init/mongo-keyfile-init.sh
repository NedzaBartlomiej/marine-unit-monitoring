#!/bin/bash

echo "#### MONGO-KEYFILE-INIT.SH ####"

mng_rs_key_path="/usr/mongo-rs-auth/mongo-rs.key"
mng_rs_dir_path="/usr/mongo-rs-auth/"

errmsg_inst_exist="Instance already exist."
errmsg_not_found="Instance not found."

echo "-- Creating dir for the keys files. --"
if [ ! -f "$mng_rs_dir_path" ]; then
  mkdir "$mng_rs_dir_path"
else
  echo "$errmsg_inst_exist"
fi


echo "-- Creating the keys file. --"
if [ ! -f "$mng_rs_key_path" ]; then
  touch "$mng_rs_key_path"
else
  echo "$errmsg_inst_exist"
fi


echo "-- Generating and inserting into file, the new keys. --"
if [ -s "$mng_rs_key_path" ]; then
  truncate --size 0 "$mng_rs_key_path"
fi
openssl rand -base64 756 > "$mng_rs_key_path"


echo "-- Changing permissions using chmod 400. --"
if [ -f "$mng_rs_key_path" ]; then
  chmod 400 "$mng_rs_key_path"
else
  echo "$errmsg_not_found"
fi