# Etapa de construcción
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa de ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Cloud Run inyecta la variable PORT (típicamente 8080). En local, default 8080.
ENV PORT=8080
EXPOSE 8080

# El application.properties ya lee server.port=${PORT:8080}, así que respeta
# la env. -Dserver.address=0.0.0.0 fuerza bind público dentro del contenedor.
ENTRYPOINT ["sh", "-c", "java -Dserver.address=0.0.0.0 -jar app.jar"]
