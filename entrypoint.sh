#!/usr/bin/env bash

# Heroku
if [[ -z $SPRING_DATASOURCE_URL ]] && [[ ! -z $DATABASE_URL ]]; then
  export SPRING_DATASOURCE_URL="$(echo "$DATABASE_URL" | sed -n 's!.*@\(.*\):5432/\(.*\)!jdbc:postgresql://\1/\2!p')"
  export SPRING_DATASOURCE_USERNAME="$(echo "$DATABASE_URL" | sed -n 's!postgres://\([^:]*\):.*!\1!p')"
  export SPRING_DATASOURCE_PASSWORD="$(echo "$DATABASE_URL" | sed -n 's!postgres://[^:]*:\(.*\)@.*!\1!p')"
  export SERVER_PORT="$PORT"
fi

exec java $JAVA_OPTS -jar /*.jar
