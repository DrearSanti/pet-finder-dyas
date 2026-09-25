# Pet Finder — AGENTS.md

Aplicación Spring Boot 4.1.1 (Java 17) con arquitectura hexagonal; las instrucciones completas están en `CLAUDE.md` y el plan en `blueprints/pet-finder-corte-2/`.

| Para qué | Comando |
|---|---|
| Unitarias + ArchUnit | `./mvnw -q test` |
| Todo (unitarias, integración, sistema, cobertura) | `./mvnw -q clean verify` |
| App web | `./mvnw spring-boot:run` → http://localhost:8080 |
| Demo de consola | `./mvnw -q compile exec:java` |
| Humo del jar | `./mvnw -q -DskipTests package && sh scripts/humo.sh` |

En PowerShell usa `.\mvnw.cmd`; en Git Bash, igual que en macOS.

**Innegociables**
- El agente nunca hace commit, tag, push ni `reset --hard`: lo hace la persona responsable de la épica.
- Todo en español; `domain` y `application` sin Spring; solo `adaptadores.salida.ia` usa `com.anthropic`.
- Nunca leas `.env`. Las pruebas nunca llaman a Claude (`ANTHROPIC_API_KEY=`).
- Una tarea está hecha solo si su `Verify` y `./mvnw -q clean verify` salen 0.

Lee `CLAUDE.md` antes de cambiar cualquier cosa.
