FROM azul/zulu-openjdk:11

RUN set -e ;\
    export DEBIAN_FRONTEND=noninteractive ;\
    apt-get update -y;\
    apt-get install -y locales ;\
    locale-gen en_US.UTF-8 ;\
    /usr/sbin/update-locale LANG=en_US.UTF-8

RUN apt-get install -y gringo

ENV LANG=en_US.UTF-8 \
  LANGUAGE=en_US.UTF-8 \
  LC_ALL=en_US.UTF-8

EXPOSE 8080 8888 11210

ADD service/target/service*.jar service.jar

COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
