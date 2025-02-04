FROM gradle:8.12.1-jdk21 AS build-stage

ARG GITHUB_USER
ARG GITHUB_TOKEN

WORKDIR /home/gradle
COPY . .

RUN gradle clean build -x test -x integrationTest --no-daemon -Pgithub.user=${GITHUB_USER} -Pgithub.token=${GITHUB_TOKEN}

FROM azul/zulu-openjdk:21

COPY --from=build-stage /home/gradle/build/libs/shelf-*.jar /shelf.jar


ENTRYPOINT ["java","-jar", "/shelf.jar" ]
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit