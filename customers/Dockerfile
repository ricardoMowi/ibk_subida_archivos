FROM adoptopenjdk/openjdk11:alpine-jre
ARG JAR_FILE=target/*.jar 
COPY ${JAR_FILE} customers.jar
ENTRYPOINT ["java", "-jar", "/customers.jar"]