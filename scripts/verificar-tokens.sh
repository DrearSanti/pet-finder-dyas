#!/usr/bin/env sh
# Verifica que la interfaz respete el sistema de diseño:
#  1. cada token definido en docs/diseno/DESIGN.md §13 existe en
#     src/main/resources/static/css/tokens.css;
#  2. src/main/resources/static/css/app.css no tiene colores hexadecimales
#     (los colores solo se usan a través de los tokens).
# Sale con 0 si ambas cosas se cumplen.
set -u
DISENO="docs/diseno/DESIGN.md"
TOKENS="src/main/resources/static/css/tokens.css"
APP="src/main/resources/static/css/app.css"
for f in "$DISENO" "$TOKENS" "$APP"; do
  [ -f "$f" ] || { echo "No existe: $f"; exit 2; }
done
estado=0
nombres=$(sed -n '/^## 13\./,/^## 14\./p' "$DISENO" | grep -oE -- '--[a-z0-9-]+:' | sort -u)
[ -n "$nombres" ] || { echo "No encontré tokens en la sección 13 de $DISENO"; exit 2; }
for nombre in $nombres; do
  if ! grep -qF -- "$nombre" "$TOKENS"; then
    echo "Falta el token en $TOKENS: ${nombre%:}"
    estado=1
  fi
done
hex=$(grep -cE '#[0-9A-Fa-f]{3,8}([^0-9A-Za-z_-]|$)' "$APP" || true)
if [ "$hex" != "0" ]; then
  echo "$APP tiene $hex línea(s) con colores hexadecimales; usa var(--token):"
  grep -nE '#[0-9A-Fa-f]{3,8}([^0-9A-Za-z_-]|$)' "$APP"
  estado=1
fi
exit "$estado"
