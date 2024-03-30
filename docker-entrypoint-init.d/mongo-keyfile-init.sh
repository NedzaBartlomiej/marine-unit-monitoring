#!/bin/sh

mng_rs_key_path="/usr/mongo-rs-auth/mongo-rs.key"
mng_rs_dir_path="/usr/mongo-rs-auth/"

errmsg_inst_exist="Instance already exist."
errmsg_not_found="Instance not found."

echo "---- 'mongo-keyfile-init.sh' ----"

echo "#### CREATING DIR 'mongo-rs-auth' ####"
if [ ! -f "$mng_rs_dir_path" ]; then
  mkdir "$mng_rs_dir_path"
else
  echo "$errmsg_inst_exist"
fi


echo "#### CREATING 'mongo-rs - keyFile' ####"
if [ ! -f "$mng_rs_key_path" ]; then
  touch "$mng_rs_key_path"
else
  echo "$errmsg_inst_exist"
fi


echo "#### GENERATING NEW KEYS FOR 'mongo-rs - keyFile' ####"
if [ -s "$mng_rs_key_path" ]; then
  truncate --size 0 "$mng_rs_key_path"
fi
openssl rand -base64 756 > "$mng_rs_key_path"


echo "#### CHANGING PERMISSIONS TO CHMOD 400 FOR 'mongo-rs - keyFile' ####"
if [ -f "$mng_rs_key_path" ]; then
  chmod 400 "$mng_rs_key_path"
else
  echo "$errmsg_not_found"
fi