---
paths:
  - "src/main/resources/static/**"
---

# Interfaz (HTML, CSS y JS)

- Antes de tocar nada aquí, la skill `diseno-pet-finder`: lee `docs/diseno/DESIGN.md` y mira `docs/diseno/vistas.html`.
- Colores, radios, sombras y tipografía **solo** con variables de `css/tokens.css`. Cero valores hex en `css/app.css` (`sh scripts/verificar-tokens.sh` lo revisa).
- El logo es `img/marca.svg`, copia byte a byte de `docs/diseno/logo/marca.svg`. Nunca se redibuja ni se edita.
- JavaScript en módulos ES sin framework: `js/api.js` (única puerta al servidor), `js/voz.js`, `js/tarjeta-viva.js`, `js/mapa.js`, `js/app.js`. Imports relativos (`./api.js`).
- Texto que viene del servidor o del usuario se inserta con `textContent`. Nunca `innerHTML` con datos.
- Nunca `alert()`: los mensajes van a `data-prueba="mensaje"` con `aria-live="polite"`.
- Los ganchos `data-prueba="..."` de la tabla de §6 del blueprint son contrato de las pruebas de UI: no los renombres ni los quites.
- La voz siempre tiene respaldo: el campo de texto está visible y hace lo mismo que el micrófono.
- Mapa: Leaflet 1.9.4 por CDN con teselas de CARTO y la atribución `© OpenStreetMap` y `© CARTO`.
- `sw.js` nunca guarda en caché nada bajo `/api/`.
- Objetivos táctiles de 44 × 44 px mínimo, foco visible, `prefers-reduced-motion` respetado.
