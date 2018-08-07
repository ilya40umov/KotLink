FROM openjdk:8u171-jdk
MAINTAINER Illia Sorokoumov <illia.sorokoumov@gmail.com>

COPY build/libs/kotlink.jar /opt/kotlink/kotlink.jar

EXPOSE 8080

USER www-data:www-data

CMD ["java", "-server", "-Xmx1024m", "-Xms1024m", "-Dspring.profiles.active=prod", "-jar", "/opt/kotlink/kotlink.jar"]