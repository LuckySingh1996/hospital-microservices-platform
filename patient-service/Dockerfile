FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/patient-service-0.0.1.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
