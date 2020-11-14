FROM maven:3.6.0-jdk-11-slim AS build

COPY src /home/bot/src
COPY pom.xml /home/bot

RUN mvn -f /home/bot/pom.xml clean package

FROM openjdk

ENV PORT 8080
COPY --from=build /home/bot/target/WakeBot-1.0-SNAPSHOT.jar /usr/local/bin

WORKDIR /usr/local/bin

RUN chmod +x WakeBot-1.0-SNAPSHOT.jar

EXPOSE $PORT

CMD ["/usr/bin/java","-jar","-Dspring.profiles.active=docker","WakeBot-1.0-SNAPSHOT.jar"]