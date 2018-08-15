#!/usr/bin/env bash

echo "Usage: $0 [port] [submodule]"

if [ -n "$1" ]; then
  port="$1"
else
  port=8080
fi

submodule="${2:-janusgraph}"
filename_prefix="webserver_${submodule}.local-${port}"

killed=""
# kill servers on specific port (had to be started from the same user
if [ -e "${filename_prefix}.pid" ]; then
  pid=$(cat "${filename_prefix}.pid")
  if [ -n "${pid}" ]; then
    echo "killing old instance ($pid) ..."
    kill ${pid} && \
      sleep 2 && \
      rm "${filename_prefix}.pid" && \
      killed="${pid}"
  else
    rm "${filename_prefix}.pid";
  fi
fi

port_pid="$(lsof -n -i ":${port}" | grep LISTEN | sed 's| \+|\t|g' | cut -f2)"
if [ -n "${port_pid}" ]; then
  echo "killing port pid ($port_pid) ..."
  kill "${port_pid}"&& \
      killed="${port_pid}"
  sleep 2
fi

if [ -z "${killed}" ]; then
  echo "Cannot find PID of a running server on port ${port}"
  echo "Try: sudo lsof -n -i ":${port}" | grep LISTEN"
fi
