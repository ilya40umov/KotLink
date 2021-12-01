FROM eclipse-temurin:11.0.13_8-jre-alpine

LABEL maintainer="illia.sorokoumov@gmail.com"

COPY build/libs/kotlink.jar /opt/kotlink/kotlink.jar

EXPOSE 8080

RUN (getent group www-data || addgroup -S www-data) && adduser -S www-data -G www-data
USER www-data:www-data

CMD ["java", "-server", "-Xmx1024m", "-Xms1024m", "-Dspring.profiles.active=prod", "-jar", "/opt/kotlink/kotlink.jar"]