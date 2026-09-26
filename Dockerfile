# Pet Finder en un contenedor, en dos etapas: la primera compila con el JDK y la segunda solo
# corre el jar con el JRE, así la imagen final no arrastra Maven ni el código fuente.

# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:17-jdk AS compilar
WORKDIR /app

# El wrapper primero: descarga Maven una sola vez y esa capa se reutiliza mientras no cambie.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# Clave gratuita de CARTO para las teselas del mapa (opcional). Se pasa al construir:
#   docker build --build-arg PETFINDER_CLAVE_CARTO=tu-clave .
# En Render, con una variable de entorno del mismo nombre. Es una clave pública por diseño (viaja en cada
# petición de teselas del navegador) y nunca se guarda en el repositorio. Sin ella el mapa usa OpenStreetMap.
ARG PETFINDER_CLAVE_CARTO=""
RUN if [ -n "$PETFINDER_CLAVE_CARTO" ]; then \
      printf "window.PETFINDER_CLAVE_CARTO = '%s';\n" "$(printf %s "$PETFINDER_CLAVE_CARTO" | tr -cd 'A-Za-z0-9_-')" \
        > src/main/resources/static/js/config-local.js; \
    fi

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# ---------- Etapa 2: correr ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Las fechas del servidor no llevan zona horaria: con la hora de Bogotá, "hace 5 min" es cierto en Colombia.
ENV TZ=America/Bogota

# Sin permisos de administrador: si algo se filtra, no llega al sistema.
RUN useradd --system --uid 10001 --no-create-home app
COPY --from=compilar --chown=app /app/target/pet-finder.jar /app/pet-finder.jar
USER app

# Render define PORT y application.properties lo lee (server.port=${PORT:8080}).
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/pet-finder.jar"]
