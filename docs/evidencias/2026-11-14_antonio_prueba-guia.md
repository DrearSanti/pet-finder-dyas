# Prueba de la guía de implementación

Registro de la prueba de [`docs/implementacion.md`](../implementacion.md): una persona que no conoce el proyecto sigue solo la guía, desde cuentas vacías hasta el sistema funcionando, y anota cada duda. Cada duda se corrigió en la guía.

| | |
|---|---|
| **Quién la probó** | [Nombre, y si es de fuera del grupo o del equipo] |
| **Fecha** | [AAAA-MM-DD, hora de Colombia] |
| **Versión de la guía** | [Commit de `docs/implementacion.md` con el que se probó, por ejemplo `abc1234`] |
| **Equipo y sistema** | [Windows 11 con Git Bash / macOS / Linux; versión de Docker] |
| **Cuentas** | [Fork en GitHub `<usuario>/pet-finder-dyas`; cuenta nueva de Render: sí o no] |
| **Duración total** | [Minutos, desde el fork hasta la prueba de humo en verde] |
| **Resultado** | [Llegó al sistema funcionando: sí o no. URL de su servicio en Render] |

## Recorrido

| Parte de la guía | ¿Funcionó como dice? | Tiempo | Comentario |
|---|---|---|---|
| 5.1 Copia del repositorio | [Sí / No] | [min] | |
| 5.2 Probar en local con Docker | | | |
| 5.3 Ajustar los workflows a tu URL | | | |
| 5.4 Primera imagen en GHCR | | | |
| 5.5 Base de datos en Render | | | |
| 5.6 Servicio web en Render | | | |
| 5.7 Deploy hook y despliegue continuo | | | |
| 5.9 Datos iniciales y primer administrador | | | |
| 6. Verificación (salud, `humo-flujo.sh` y recorrido en el navegador) | | | |

## Dudas y correcciones

Una fila por cada vez que la persona tuvo que preguntar, adivinar o buscar fuera de la guía.

| # | Parte | Qué no estaba claro o qué falló | Cómo se resolvió | Corrección en la guía (commit) |
|---|---|---|---|---|
| 1 | | | | |

## Evidencia

- Salida de `sh scripts/humo-flujo.sh https://<su-url-de-render>`:
  ```text
  [Pegar aquí las seis líneas "ok" y la última línea]
  ```
- Corrida del workflow `CD` en su fork: [enlace `https://github.com/<usuario>/pet-finder-dyas/actions/runs/<id>`]
- Capturas, si las hay: [nombres de archivo en `docs/evidencias/`]
