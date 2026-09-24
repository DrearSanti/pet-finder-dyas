---
name: diseno-pet-finder
description: Sistema de diseño de Pet Finder. Úsalo SIEMPRE antes de crear o modificar cualquier interfaz, pantalla, componente, estilo CSS, ícono, texto visible o recurso gráfico de Pet Finder (página web, PWA, correos, presentaciones o capturas). Se activa con "pantalla", "vista", "UI", "interfaz", "CSS", "estilos", "diseño", "botón", "tarjeta", "mapa", "logo", "ícono", "PWA", "index.html", "estilos.css".
---

# Diseño de Pet Finder

Pet Finder tiene un sistema de diseño fijo: premium estilo Apple, calidez editorial de pampam.city y el logo "Órbita abierta". Todo lo visual debe salir de él.

## Antes de tocar cualquier interfaz

1. Lee `docs/diseno/DESIGN.md` completo. Es la fuente de verdad: tokens, tipografía, componentes, mapa, movimiento, PWA y accesibilidad.
2. Abre la vista correspondiente en `docs/diseno/vistas.html` y replica su estructura. Las clases CSS de ese archivo (`.btn-primario`, `.estado`, `.tarjeta-viva`, `.campo`, `.pregunta`, `.hoja`, `.pestanas`, `.pin`…) son las que usa la app.
3. Usa el logo solo desde `docs/diseno/logo/` (`marca.svg`, `marca-pequena.svg` por debajo de 24 px, `icono-app.svg`). No lo redibujes.

## Reglas que no se rompen

- Solo variables CSS de `DESIGN.md` §13. Ningún hexadecimal nuevo dentro de un componente.
- Un solo botón primario (Tinta) por pantalla.
- Colores de estado (perdida, avistamiento, encontrada, resuelto) solo para comunicar estado, y siempre con su palabra al lado.
- Tipografías: Geist (interfaz), Instrument Serif (máximo un titular por pantalla), Geist Mono (identificadores).
- Textos en español, tuteando, con la voz de `DESIGN.md` §2.3. Sin emojis ni exclamaciones del sistema.
- Nunca mostrar teléfono, correo ni dirección exacta en listas o en el mapa.
- Nada se publica sin confirmación explícita de la persona.
- Modo claro y oscuro, de 375 px a 1440 px, contraste AA y objetivos táctiles de 44 px.

## Al terminar

Recorre la lista de revisión de `DESIGN.md` §15. Si el cambio introduce un patrón nuevo, agrégalo primero a `DESIGN.md` y a `vistas.html`, en el mismo commit.
