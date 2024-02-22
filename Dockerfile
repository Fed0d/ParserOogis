FROM maven:3.8.1-openjdk-8 AS build

WORKDIR /app

COPY ./build ./build
COPY ./mr-231 ./mr-231
COPY ./mr-231-3 ./mr-231-3
COPY ./startApp ./startApp

WORKDIR /app/build

RUN mvn clean install

FROM openjdk:8-jre-slim

COPY --from=build /app/startApp/target/startApp-1.0-SNAPSHOT.jar /app/startApp.jar
COPY --from=build /app/mr-231/target/mr-231-1.0-SNAPSHOT.jar /app/mr-231.jar
COPY --from=build /app/mr-231-3/target/mr-231-3-1.0-SNAPSHOT.jar /app/mr-231-3.jar

WORKDIR /app

CMD ["java", "-jar", "startApp.jar"]