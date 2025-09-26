FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/shop-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]