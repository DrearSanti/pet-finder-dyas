#!/usr/bin/env python3
"""Umbral de SAST (S6): CodeQL solo reporta, así que este paso lee sus SARIF y falla
si queda un hallazgo con security-severity igual o mayor al umbral sin tratar.

Uso: python3 scripts/umbral-sarif.py CARPETA_O_ARCHIVO [UMBRAL] [EXCEPCIONES]
  UMBRAL       7.0 por defecto (7.0 a 8.9 es alto; 9.0 o más, crítico).
  EXCEPCIONES  .github/codeql/excepciones.txt por defecto. Una por línea: "regla ruta",
               por ejemplo "java/ssrf src/main/java/petfinder/Foo.java". Cada línea
               tiene su fila en docs/seguridad.md. "#" empieza un comentario.

Sale 0 si no hay hallazgos por encima del umbral, 1 si los hay y 2 si no encontró SARIF:
una ruta mal escrita no puede dejar pasar el pipeline en verde.
"""
import json
import pathlib
import sys


def leer_excepciones(ruta):
    excepciones = set()
    archivo = pathlib.Path(ruta)
    if not archivo.is_file():
        return excepciones
    for linea in archivo.read_text(encoding="utf-8").splitlines():
        linea = linea.split("#", 1)[0].strip()
        if linea:
            partes = linea.split()
            if len(partes) != 2:
                sys.exit(f"Excepción mal escrita (se espera 'regla ruta'): {linea}")
            excepciones.add((partes[0], partes[1]))
    return excepciones


def severidades(run):
    # Las reglas de los paquetes de consultas quedan en extensions, no en driver.
    reglas = list(run.get("tool", {}).get("driver", {}).get("rules", []))
    for extension in run.get("tool", {}).get("extensions", []):
        reglas.extend(extension.get("rules", []))
    mapa = {}
    for regla in reglas:
        valor = regla.get("properties", {}).get("security-severity")
        if valor is not None:
            mapa[regla.get("id")] = float(valor)
    return mapa


def ruta_de(resultado):
    for ubicacion in resultado.get("locations", []):
        uri = ubicacion.get("physicalLocation", {}).get("artifactLocation", {}).get("uri")
        if uri:
            return uri
    return "?"


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    origen = pathlib.Path(sys.argv[1])
    umbral = float(sys.argv[2]) if len(sys.argv) > 2 else 7.0
    excepciones = leer_excepciones(sys.argv[3] if len(sys.argv) > 3 else ".github/codeql/excepciones.txt")

    archivos = sorted(origen.rglob("*.sarif")) if origen.is_dir() else [origen]
    archivos = [a for a in archivos if a.is_file()]
    if not archivos:
        print(f"No se encontró ningún SARIF en {origen}")
        return 2

    bloqueantes = 0
    for archivo in archivos:
        sarif = json.loads(archivo.read_text(encoding="utf-8"))
        for run in sarif.get("runs", []):
            mapa = severidades(run)
            for resultado in run.get("results", []):
                regla = resultado.get("ruleId") or resultado.get("rule", {}).get("id", "?")
                severidad = mapa.get(regla, 0.0)
                ruta = ruta_de(resultado)
                if severidad < umbral:
                    continue
                if resultado.get("suppressions"):
                    print(f"Suprimido en el código: {regla} {ruta} ({severidad})")
                    continue
                if (regla, ruta) in excepciones:
                    print(f"Excepción registrada en docs/seguridad.md: {regla} {ruta} ({severidad})")
                    continue
                bloqueantes += 1
                linea = (resultado.get("locations") or [{}])[0].get("physicalLocation", {}).get("region", {}).get("startLine", "?")
                print(f"BLOQUEA: {regla} en {ruta}:{linea}, security-severity {severidad}")

    print(f"{bloqueantes} hallazgo(s) con security-severity >= {umbral} sin tratar en {len(archivos)} SARIF.")
    return 1 if bloqueantes else 0


if __name__ == "__main__":
    sys.exit(main())
