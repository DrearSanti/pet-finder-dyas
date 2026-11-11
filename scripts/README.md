# Scripts

Comprobaciones que se corren desde la raíz del repositorio, en macOS, Linux o Git Bash (en Windows, desde Git Bash). Todas salen con 0 si la comprobación pasa y con otro código si falla, para que sirvan igual a mano y en el pipeline.

| Script | Qué comprueba | Uso |
|---|---|---|
| [`humo.sh`](humo.sh) | Arranca el jar empaquetado, espera a que `/actuator/health` responda 200, pide cada ruta que reciba y apaga el servidor | `./mvnw -q -DskipTests package` y luego `sh scripts/humo.sh / /api/reportes` |
| [`humo-docker.sh`](humo-docker.sh) | Construye la imagen con el `Dockerfile`, la arranca, espera `/actuator/health` = 200 y la detiene. Necesita Docker en marcha | `sh scripts/humo-docker.sh` |
| [`contiene.sh`](contiene.sh) | Que un archivo exista, tenga cada texto requerido y no tenga los prohibidos (los que empiezan con `!`) | `sh scripts/contiene.sh README.md 'Cómo ejecutar' '!__'` |
| [`verificar-tokens.sh`](verificar-tokens.sh) | Que la interfaz respete el sistema de diseño: los tokens de `docs/diseno/DESIGN.md` existen en `tokens.css` y `app.css` no tiene colores sueltos | `sh scripts/verificar-tokens.sh` |
| [`umbral-sarif.py`](umbral-sarif.py) | Umbral de SAST: lee los SARIF de CodeQL y falla si queda un hallazgo con `security-severity` de 7,0 o más que no esté en `.github/codeql/excepciones.txt`. Sale 2 si no encuentra SARIF | `python3 scripts/umbral-sarif.py sarif-results 7.0 .github/codeql/excepciones.txt` |

Quien agregue un script nuevo (por ejemplo `humo-flujo.sh` o `umbral-sarif.py` del Corte 3) agrega su fila aquí.
