#!/usr/bin/env sh
# Prueba de humo del flujo principal contra una URL ya desplegada (Compose, Render o un jar local).
# A diferencia de humo.sh y humo-docker.sh, no arranca nada: recibe la URL y recorre lo que hace
# una persona: el servicio está listo, se publica un caso, se ve su detalle y aparece en la lista.
# También exige que la consola de H2 no responda (amenaza T9) y cierra el caso que creó, para no
# dejar reportes de prueba en el mapa.
#
# Uso (macOS, Linux y Git Bash de Windows):
#   sh scripts/humo-flujo.sh http://localhost:8080
# Variables opcionales: HUMO_ESPERA (segundos que espera la salud, 120 por defecto).
# Sale con 0 solo si todos los pasos dieron lo esperado; con 1 en el primer paso que falla.
set -u

if [ "$#" -ne 1 ]; then
  echo "Uso: sh scripts/humo-flujo.sh URL_BASE   (por ejemplo http://localhost:8080)"
  exit 2
fi
BASE="${1%/}"
ESPERA="${HUMO_ESPERA:-120}"
CUERPO="$(mktemp)"
trap 'rm -f "$CUERPO"' EXIT

# Hace una petición, deja el cuerpo en $CUERPO e imprime solo el código HTTP (000 si no hubo respuesta).
pedir() {
  curl -s -o "$CUERPO" -w '%{http_code}' --max-time 20 "$@" || true
}

fallar() {
  echo "FALLA: $1"
  echo "Cuerpo recibido: $(head -c 300 "$CUERPO" 2>/dev/null)"
  exit 1
}

# 1. Salud: un plan gratis puede estar dormido, así que se espera antes de dar el servicio por caído.
codigo=000
segundos=0
while [ "$segundos" -lt "$ESPERA" ]; do
  codigo=$(pedir "$BASE/actuator/health/readiness")
  [ "$codigo" = "200" ] && break
  sleep 5
  segundos=$((segundos + 5))
done
[ "$codigo" = "200" ] || fallar "/actuator/health/readiness respondió $codigo después de ${ESPERA} s"
echo "ok  GET /actuator/health/readiness -> 200"

# 2. La consola de H2 no se ofrece en un entorno desplegado (amenaza T9). Va antes de crear
# el caso para que, si falla, no quede un reporte de prueba abierto.
codigo=$(pedir "$BASE/h2-console/")
[ "$codigo" = "404" ] || fallar "GET /h2-console/ respondió $codigo (se esperaba 404)"
echo "ok  GET /h2-console/ -> 404"

# 3. Publicar un caso con un nombre que no puede existir: así el detalle se compara sin suponer un id.
NOMBRE="Humo$(date +%s)"
codigo=$(pedir -X POST "$BASE/api/reportes" -H 'Content-Type: application/json' -d "{
  \"tipo\": \"PERDIDA\", \"zona\": \"Prueba de humo\", \"referencia\": \"Pipeline\",
  \"descripcion\": \"Caso creado por scripts/humo-flujo.sh; se cierra al terminar\",
  \"nombreContacto\": \"Prueba de humo\", \"medioContacto\": \"3000000000\",
  \"mascota\": { \"nombre\": \"$NOMBRE\", \"especie\": \"perro\" } }")
[ "$codigo" = "201" ] || fallar "POST /api/reportes respondió $codigo (se esperaba 201)"
ID=$(grep -o '"id":"[^"]*"' "$CUERPO" | head -n 1 | cut -d '"' -f 4)
[ -n "$ID" ] || fallar "POST /api/reportes no devolvió un id"
echo "ok  POST /api/reportes -> 201 ($ID)"

# 4. El detalle devuelve la misma mascota que se publicó.
codigo=$(pedir "$BASE/api/reportes/$ID")
[ "$codigo" = "200" ] || fallar "GET /api/reportes/$ID respondió $codigo"
grep -q "\"nombreMascota\":\"$NOMBRE\"" "$CUERPO" || fallar "GET /api/reportes/$ID no trae la mascota $NOMBRE"
echo "ok  GET /api/reportes/$ID -> 200 con $NOMBRE"

# 5. La lista pública incluye el caso nuevo.
codigo=$(pedir "$BASE/api/reportes")
[ "$codigo" = "200" ] || fallar "GET /api/reportes respondió $codigo"
grep -q "\"id\":\"$ID\"" "$CUERPO" || fallar "GET /api/reportes no incluye $ID"
echo "ok  GET /api/reportes -> 200 e incluye $ID"

# 6. Limpieza: se cierra el caso de prueba para que no quede en la lista ni en el mapa.
codigo=$(pedir -X POST "$BASE/api/reportes/$ID/cerrar")
[ "$codigo" = "204" ] || fallar "POST /api/reportes/$ID/cerrar respondió $codigo (se esperaba 204)"
echo "ok  POST /api/reportes/$ID/cerrar -> 204"

echo "Prueba de humo del flujo: todo en orden contra $BASE"
