FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

# Copy the JAR file
COPY target/conexoes-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java","-jar","app.jar"]