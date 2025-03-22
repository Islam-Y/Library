FROM maven:3.9.9-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn -B clean package -DskipTests

FROM tomcat:9.0.102-jdk21-temurin-jammy
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080