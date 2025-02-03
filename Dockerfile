FROM gradle:8.12.0-jdk21 AS build-stage

WORKDIR /home/gradle
COPY . .

RUN gradle clean build -x test --no-daemon

FROM azul/zulu-openjdk:23

COPY --from=build-stage /home/gradle/build/libs/shelf-*.jar /shelf.jar


ENTRYPOINT ["java","-jar", "/shelf.jar" ]
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit