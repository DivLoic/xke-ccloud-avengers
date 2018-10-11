FROM openjdk:8u181

MAINTAINER Loïc DIVAD ldivad@xebia.fr

RUN mkdir -p /etc/ccloud-streams/ /opt/

COPY ccloud-streams*.tar /tmp/

RUN tar -xf /tmp/ccloud-streams*.tar -C /opt/

WORKDIR /opt/ccloud-streams/

ENTRYPOINT [ "/opt/ccloud-streamsg/bin/ccloud-streams" ]