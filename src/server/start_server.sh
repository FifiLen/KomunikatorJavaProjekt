#!/bin/bash

# Start Cloud SQL Proxy
./cloud_sql_proxy -instances=projektjava:europe-central2:bazajava=tcp:3306 &

# Give Cloud SQL Proxy a few seconds to start
sleep 5

# Start the Java server
java -cp .:libs/mysql-connector-j-8.4.0.jar:. server.ChatServer
