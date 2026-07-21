
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app


COPY --chown=gradle:gradle . .

RUN chmod +x ./gradlew || true
RUN ./gradlew bootJar --no-daemon || \
    (apt-get update && apt-get install -y gradle && gradle bootJar --no-daemon)


FROM eclipse-temurin:21-jre

WORKDIR /app


COPY --from=builder /app/build/libs/*.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]