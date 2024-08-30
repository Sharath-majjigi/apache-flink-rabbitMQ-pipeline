FROM gradle:jdk21 as build
WORKDIR /app

COPY build.gradle settings.gradle /app/
COPY src /app/src

RUN gradle clean build -x test --no-daemon

FROM openjdk:21
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/application.jar

EXPOSE 8080

ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "/app/application.jar"]
