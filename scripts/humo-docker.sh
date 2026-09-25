#!/usr/bin/env sh
# Prueba de humo del contenedor: construye la imagen con el Dockerfile del
# proyecto, la arranca, espera /actuator/health = 200 y la detiene.
# Requiere Docker en marcha. Sale con 0 solo si el contenedor respondió 200.
set -u
IMAGEN="pet-finder:humo"
NOMBRE="pet-finder-humo"
PUERTO="${PUERTO_HUMO_DOCKER:-18090}"
docker build -q -t "$IMAGEN" . >/dev/null || { echo "docker build falló"; exit 1; }
docker rm -f "$NOMBRE" >/dev/null 2>&1 || true
docker run -d --rm --name "$NOMBRE" -p "$PUERTO:8080" -e PORT=8080 "$IMAGEN" >/dev/null || { echo "docker run falló"; exit 1; }
codigo=000
intentos=0
while [ "$intentos" -lt 120 ]; do
  codigo=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$PUERTO/actuator/health" || true)
  [ "$codigo" = "200" ] && break
  intentos=$((intentos + 1))
  sleep 1
done
[ "$codigo" = "200" ] || docker logs "$NOMBRE" 2>&1 | tail -20
docker stop "$NOMBRE" >/dev/null 2>&1 || true
if [ "$codigo" = "200" ]; then
  echo "contenedor /actuator/health -> 200"
  exit 0
fi
echo "contenedor /actuator/health respondió $codigo"
exit 1
