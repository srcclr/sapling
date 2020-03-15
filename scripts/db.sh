#!/usr/bin/env bash

CONTAINER_NAME="pi-planning-db"
HOST=localhost

stop() {

  local PG_RUNNING="$(docker ps -f name=$CONTAINER_NAME | grep "$CONTAINER_NAME")"
  if [[ -n ${PG_RUNNING} ]]; then
     echo "Found $CONTAINER_NAME running. Stopping and removing."
     docker stop $CONTAINER_NAME > /dev/null 2>&1
     docker rm $CONTAINER_NAME > /dev/null 2>&1
  fi

  echo "$CONTAINER_NAME stopped and removed."
}

wait_psql() {
  printf 'Waiting'
  until psql -h "$HOST" -U postgres -c '\l' > /dev/null 2>&1; do
    printf '.'
    sleep 1
  done
  printf "\nDatabase started\n"
}

start() {
  local POSTGRES_DATA="$(PWD)/scripts/postgres_data"
  docker run -v $POSTGRES_DATA:/var/lib/postgresql/data -p 5432:5432 --name $CONTAINER_NAME --rm -d postgres:11.4
  wait_psql
}

init() {
  psql -h "$HOST" -p 5432 -U postgres < scripts/initdb.sql
}


enter() {
  psql -h "$HOST" -p 5432 -U agile -d piplanning
}

fresh() {
  stop
  start
  init
  enter
}

cmd="$1"
shift

if [ -z $cmd ]; then
  enter
else
  $cmd "$@"
fi
