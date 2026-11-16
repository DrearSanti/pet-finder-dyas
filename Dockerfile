# syntax=docker/dockerfile:1

# Pet Finder en un contenedor, en dos etapas: la primera compila con Maven y la segunda solo
# corre el .jar con el JRE, así la imagen final no arrastra Maven, el JDK ni el código fuente.

# ---------- Etapa 1: compilar el .jar con Maven (las pruebas corren antes, en el pipeline) ----------
FROM maven:3.9-eclipse-temurin-26-noble AS build
WORKDIR /src

# Primero solo el pom: las dependencias quedan en una capa propia que se reutiliza mientras el pom
# no cambie. Sin --mount=type=cache: esa caché vive solo en la máquina que construye y no viaja en
# la caché type=gha de GitHub Actions; una capa normal sí.
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline

# Después el código
COPY src ./src

RUN mvn -B -ntp -q package -DskipTests \
 && cp target/pet-finder.jar /app.jar

# ---------- Etapa 2: imagen final solo con el JRE y el .jar ----------
FROM eclipse-temurin:17.0.20.1_1-jre-noble

# Las fechas del servidor no llevan zona horaria: con la hora de Bogotá, "hace 5 min" es cierto en Colombia
ENV TZ=America/Bogota

# Qué commit y qué versión lleva la imagen. Los pasa el pipeline al construir y salen en /actuator/info,
# para comprobar que en Render corre de verdad la versión recién desplegada.
ARG PETFINDER_COMMIT=local
ARG PETFINDER_VERSION=sin-version
ENV PETFINDER_COMMIT=${PETFINDER_COMMIT} PETFINDER_VERSION=${PETFINDER_VERSION}

# Usuario sin privilegios: si alguien compromete la aplicación, no es root dentro del contenedor
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app
COPY --from=build --chown=app:app /app.jar app.jar
USER app

# Render define PORT y application.properties lo lee (server.port=${PORT:8080}); en local es 8080
EXPOSE 8080

# Docker y Compose consultan este endpoint para saber si la aplicación está lista (Spring Boot Actuator)
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
  CMD curl -fsS "http://localhost:${PORT:-8080}/actuator/health/readiness" || exit 1

# La JVM usa como máximo el 75 % de la memoria que el contenedor tenga asignada
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
