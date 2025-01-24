FROM gradle:8.12.0-jdk21 AS build-stage

WORKDIR /home/gradle
COPY . .

RUN gradle clean build --no-daemon

FROM azul/zulu-openjdk:21

COPY --from=build-stage /home/gradle/build/libs/shelf-*.jar /shelf.jar


ENTRYPOINT ["java","-jar", "/shelf.jar" ]
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit