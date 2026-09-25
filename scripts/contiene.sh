#!/usr/bin/env sh
# Verifica el contenido de un archivo de texto.
# Uso: sh scripts/contiene.sh ARCHIVO 'texto requerido' ['!texto prohibido' ...]
# Sale con 0 solo si ARCHIVO existe, contiene cada texto requerido (búsqueda
# literal) y no contiene ninguno de los que empiezan por "!". Sale con 2 si el
# archivo no existe y con 1 si falta o sobra algún texto.
# Usa comillas simples en la terminal: con dobles, zsh/bash interpretan "!".
set -u
if [ "$#" -lt 2 ]; then
  echo "Uso: sh scripts/contiene.sh ARCHIVO TEXTO [TEXTO ...]"
  exit 2
fi
archivo="$1"
shift
if [ ! -f "$archivo" ]; then
  echo "No existe: $archivo"
  exit 2
fi
estado=0
for patron in "$@"; do
  case "$patron" in
    !*)
      texto="${patron#!}"
      if grep -qF -- "$texto" "$archivo"; then
        echo "Prohibido en $archivo: $texto"
        estado=1
      fi
      ;;
    *)
      if ! grep -qF -- "$patron" "$archivo"; then
        echo "Falta en $archivo: $patron"
        estado=1
      fi
      ;;
  esac
done
exit "$estado"
