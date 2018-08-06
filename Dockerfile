FROM openjdk:8u171-jdk
MAINTAINER Illia Sorokoumov <illia.sorokoumov@gmail.com>

COPY build/libs/kotlink.jar /opt/kotlink/kotlink.jar

EXPOSE 8080

USER www-data:www-data

ENV SPRING_PROFILES_ACTIVE prod

CMD ["java", "-jar", "/opt/kotlink/kotlink.jar"]