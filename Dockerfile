FROM openjdk:21

LABEL author="Bartlomiej Nedza"

RUN mkdir /usr/src/marine-unit-monitoring
WORKDIR /usr/src/marine-unit-monitoring

COPY ./target/marine-unit-monitoring.jar /usr/src/marine-unit-monitoring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/src/marine-unit-monitoring/marine-unit-monitoring.jar"]