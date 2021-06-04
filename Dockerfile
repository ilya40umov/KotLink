FROM adoptopenjdk/openjdk11:jre-11.0.11_9-alpine

LABEL maintainer="illia.sorokoumov@gmail.com"

COPY build/libs/kotlink.jar /opt/kotlink/kotlink.jar

EXPOSE 8080

USER www-data:www-data

CMD ["java", "-server", "-Xmx1024m", "-Xms1024m", "-Dspring.profiles.active=prod", "-jar", "/opt/kotlink/kotlink.jar"]