#!/usr/bin/env sh
# Prueba de humo de Pet Finder: arranca el jar empaquetado, espera a que
# /actuator/health responda 200, pide cada ruta recibida como argumento
# (cada una debe responder 200) y apaga el servidor.
#
# Uso (desde la raíz del proyecto, en macOS, Linux o Git Bash de Windows):
#   ./mvnw -q -DskipTests package && sh scripts/humo.sh [/ruta ...]
# Sale con 0 solo si todo respondió 200.
set -u
PUERTO="${PUERTO_HUMO:-18080}"
JAR="target/pet-finder.jar"
if [ ! -f "$JAR" ]; then
  echo "No existe $JAR. Corre primero: ./mvnw -q -DskipTests package"
  exit 2
fi
mkdir -p target
java -jar "$JAR" --server.port="$PUERTO" >target/humo.log 2>&1 &
PID=$!
codigo=000
intentos=0
while [ "$intentos" -lt 90 ]; do
  codigo=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$PUERTO/actuator/health" || true)
  [ "$codigo" = "200" ] && break
  intentos=$((intentos + 1))
  sleep 1
done
estado=0
if [ "$codigo" != "200" ]; then
  echo "health respondió $codigo; últimas líneas de target/humo.log:"
  tail -20 target/humo.log
  estado=1
else
  echo "/actuator/health -> 200"
  for ruta in "$@"; do
    c=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$PUERTO$ruta" || true)
    echo "$ruta -> $c"
    [ "$c" = "200" ] || estado=1
  done
fi
kill "$PID" 2>/dev/null
wait "$PID" 2>/dev/null
exit "$estado"
