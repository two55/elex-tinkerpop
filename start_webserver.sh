#!/usr/bin/env bash

echo "Usage: $0 [port] [submodule]"

echo "Building project dependencies (ignoring tests) ..."
mvn install -DskipTests

if [ -n "$1" ]; then
  port="$1"
  opts="-Dspring-boot.run.arguments=--server.port=${port}"
else
  # default spring-boot port is 8080
  port="8080"
  opts=""
fi

submodule="${2:-janusgraph}"

filename_prefix="webserver_${submodule}.local-${port}"

# start the server in background and show output
mkdir -p log
nohup mvn -pl "${submodule}" spring-boot:run $opts > "log/${filename_prefix}.out" 2>&1 &
sleep 30 && lsof -n -i ":${port}" | grep LISTEN | sed 's| \+|\t|g' | cut -f2 > "${filename_prefix}.pid" &
echo "following maven output ..."
tail -f "log/${filename_prefix}.out"
