## Etapa de construcción
#FROM maven:3.9.6-eclipse-temurin-21 AS build
#WORKDIR /app
#COPY . .
#RUN mvn clean package -DskipTests
#
## Etapa de ejecución
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#COPY --from=build /app/target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]


# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar solo los archivos necesarios para descargar dependencias
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar el resto del código
COPY src ./src

# Compilar el proyecto sin tests
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
