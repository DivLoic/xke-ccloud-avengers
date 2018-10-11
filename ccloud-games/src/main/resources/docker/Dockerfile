FROM openjdk:8u181

MAINTAINER Loïc DIVAD ldivad@xebia.fr

RUN mkdir -p /etc/ccloud-games/ /opt/

COPY ccloud-games*.tar /tmp/

RUN tar -xf /tmp/ccloud-games*.tar -C /opt/

RUN mv /opt/ccloud-games*/ /opt/ccloud-games/

WORKDIR /opt/ccloud-games/

ENTRYPOINT [ "/opt/ccloud-games/bin/ccloud-games" ]