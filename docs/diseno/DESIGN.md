# Pet Finder — Sistema de diseño

> Fuente de verdad del estilo visual de Pet Finder. Toda pantalla, componente o recurso gráfico nuevo debe salir de este documento. Si algo no está aquí, se agrega aquí primero y después se implementa.

| Dato | Valor |
|---|---|
| Versión | 1.0 (Corte 2) |
| Referencias | Apple (restricción, un solo acento, mucho aire) + pampam.city (calidez editorial, mapas) |
| Referencia visual | [`vistas.html`](vistas.html) — todas las pantallas y componentes con estos mismos tokens |
| Figma | [Pet Finder — Diseño de la app](https://www.figma.com/design/YWCs5dxylTW9uQUYHDoZNU) (marca, fundamentos y primeras pantallas) |
| Estado del color de marca | **Provisional.** El acento azul se cambia en un solo token cuando se defina la marca |

---

## 1. Principios

1. **Premium por restricción.** Pocas cosas, bien hechas. Un solo color de acción por pantalla, mucho espacio en blanco y tipografía como protagonista. Si dudas entre agregar o quitar, quita.
2. **El mapa y la mascota son el contenido.** La interfaz se retira: vidrio, bordes finos y sombras suaves. Nada compite con la foto o el pin.
3. **Calma para alguien angustiado.** Quien perdió a su mascota está nervioso. Textos cortos y cálidos, un paso a la vez, y siempre claro qué falta.
4. **El estado se lee en un vistazo.** Pérdida, avistamiento, encontrada y resuelto tienen color propio y consistente en pines, etiquetas y tarjetas.
5. **Nada se publica sin que la persona lo vea.** La voz y el asistente llenan la tarjeta; publicar es siempre un toque explícito.

---

## 2. Marca

### 2.1 Logo — "Órbita abierta"

Dos anillos abiertos en puntos opuestos (movimiento de búsqueda) y un punto que ocupa el hueco del anillo exterior (la mascota encontrada). El punto central es quien busca.

| Archivo | Uso |
|---|---|
| [`logo/marca.svg`](logo/marca.svg) | Marca sola. Usa `currentColor`: hereda el color del texto |
| [`logo/marca-pequena.svg`](logo/marca-pequena.svg) | Versión de trazo grueso para ≤ 24 px (favicon, notificaciones, pestaña) |
| [`logo/icono-app.svg`](logo/icono-app.svg) | Ícono de app: marca blanca sobre fondo Tinta, esquinas de 26 % |

Geometría en un lienzo de 200 × 200 (no se redibuja a ojo):

| Elemento | Especificación |
|---|---|
| Punto central | círculo r = 16 en (100, 100) |
| Anillo interior | arco r = 44, trazo 13, abierto entre 120° y 150° (abajo a la izquierda) |
| Anillo exterior | arco r = 76, trazo 13, abierto entre −70° y −20° (arriba a la derecha) |
| Punto encontrado | círculo r = 12 en (153.74, 46.26), centrado en el hueco del anillo exterior |
| Extremos | redondeados (`stroke-linecap: round`) |

Reglas:
- Área de respeto: la mitad del diámetro del punto central alrededor de toda la marca.
- Tamaño mínimo: 16 px, siempre con `marca-pequena.svg` por debajo de 24 px.
- Solo en Tinta sobre claro o en Blanco sobre oscuro. No se rellena con degradados, no se rota, no se le agregan sombras ni se deforma.
- El punto encontrado puede animarse (ver §9), la geometría nunca.

### 2.2 Nombre

- Escritura: **Pet *Finder*** — "Pet" en Geist Medium y "*Finder*" en Instrument Serif itálica, un 15 % más grande que "Pet".
- En espacios mínimos (barra, favicon con texto) se usa solo la marca, nunca "PF".
- En textos corridos se escribe "Pet Finder", sin estilos.

### 2.3 Voz y tono

| Regla | Sí | No |
|---|---|---|
| Tutear, cálido y breve | "Cuéntanos qué pasó" | "Por favor, ingrese los datos del evento" |
| Botones con verbo primero, 1 a 3 palabras | "Publicar reporte", "La vi", "Compartir" | "Enviar", "OK", "Click aquí" |
| Errores: qué pasó y qué hacer | "Ese reporte ya se cerró. Busca otro caso activo." | "Error 409: operación no permitida" |
| Sin signos de exclamación en mensajes del sistema | "Reporte publicado" | "¡¡Reporte publicado con éxito!!" |
| Estados vacíos como invitación | "Aún no hay casos cerca. Si viste una mascota, cuéntanos." | "No hay datos" |
| Nunca prometer lo que el sistema no sabe | "Posible coincidencia" | "¡Es tu mascota!" |

---

## 3. Color

Todos los colores se consumen como variables CSS. **Nunca se escribe un hexadecimal dentro de un componente.**

### 3.1 Neutros y acento

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `--color-fondo` | `#F5F5F7` Perla | `#0B0B0C` | Lienzo de la página |
| `--color-superficie` | `#FFFFFF` | `#1C1C1E` | Tarjetas, hojas, barras |
| `--color-superficie-2` | `#F5F5F7` | `#2C2C2E` | Tarjetas dentro de una hoja, campos |
| `--color-tinta` | `#0B0B0C` | `#F5F5F7` | Texto principal, botón primario |
| `--color-texto-2` | `#6E6E73` | `#A1A1A6` | Texto secundario |
| `--color-texto-3` | `#86868B` | `#8E8E93` | Metadatos, etiquetas, marcadores de posición |
| `--color-borde` | `rgba(0,0,0,.08)` | `rgba(255,255,255,.10)` | Líneas finas y divisores |
| `--color-acento` | `#0071E3` | `#0A84FF` | Enlaces y selección. **Provisional** |
| `--color-sobre-tinta` | `#FFFFFF` | `#0B0B0C` | Texto encima del botón primario |

### 3.2 Estados del reporte

Cada estado tiene un color de relleno (pines, puntos, fondos tenues) y un color de texto que cumple contraste AA sobre fondo claro.

| Estado | Relleno claro | Texto claro | Relleno oscuro | Texto oscuro |
|---|---|---|---|---|
| Perdida | `#FF5F1F` | `#C2410C` | `#FF7A45` | `#FF9A70` |
| Avistamiento | `#F5A300` | `#8A5A00` | `#FFB31A` | `#FFC94D` |
| Encontrada | `#7A5AF8` | `#5B3FD6` | `#9580FF` | `#B3A3FF` |
| Resuelto | `#1DB954` | `#0F7A36` | `#30D158` | `#5EE082` |

Tokens: `--estado-perdida`, `--estado-perdida-texto`, y así para cada estado. El fondo tenue de una etiqueta es el relleno al 12 % (`color-mix(in srgb, var(--estado-perdida) 12%, transparent)`).

### 3.3 Reglas de color

- **Un solo botón oscuro (primario) por pantalla.** Lo demás es secundario (Perla) o texto.
- Los colores de estado **solo** comunican estado. No se usan como decoración ni como acento de marca.
- Los campos de la tarjeta viva usan un punto de color para mostrar de qué parte de la frase salió cada dato. Esos colores son los de estado y el acento; no se agregan otros.
- Texto sobre un fondo de color: siempre la variante `-texto` del mismo color, nunca gris ni negro.

---

## 4. Tipografía

Familias (Google Fonts, gratuitas):

| Familia | Rol |
|---|---|
| **Geist** | Toda la interfaz |
| **Instrument Serif** | Titulares editoriales (nombre de la mascota, encabezados grandes). Máximo uno por pantalla |
| **Geist Mono** | Identificadores, etiquetas técnicas, contadores (`PF-001`, `ESCUCHANDO…`) |

Escala (tamaño / peso / interlineado / tracking):

| Token | Uso | Especificación |
|---|---|---|
| `--tipo-display` | Nombre de la mascota en el detalle, titular de portada | Instrument Serif 52 / 400 / 1.05 / −2 % |
| `--tipo-titular` | Encabezado de pantalla ("Cuéntanos qué pasó") | Instrument Serif 38 / 400 / 1.1 / −2 % |
| `--tipo-titulo-1` | Títulos de sección ("Cerca de ti") | Geist 24 / 600 / 1.2 / −3 % |
| `--tipo-titulo-2` | Nombre en tarjeta, títulos de bloque | Geist 17 / 600 / 1.3 / −2 % |
| `--tipo-cuerpo` | Texto principal | Geist 17 / 400 / 1.45 / 0 |
| `--tipo-secundario` | Metadatos importantes, botones | Geist 15 / 500 / 1.35 / 0 |
| `--tipo-pie` | Metadatos, ayudas | Geist 13 / 400 / 1.4 / 0 |
| `--tipo-etiqueta` | Identificadores, estados técnicos | Geist Mono 12 / 400 / 1.3 / +4 %, MAYÚSCULAS |

Reglas: solo pesos 400, 500 y 600. Tracking negativo solo en títulos. Nunca texto de interfaz por debajo de 12 px. Frases en minúscula inicial (sin Mayúsculas En Cada Palabra).

---

## 5. Espacio, forma y profundidad

**Espaciado** (base 4): `4, 8, 12, 16, 20, 24, 32, 48, 64`. Margen de pantalla: 20 px en móvil, 32 px en escritorio. Separación entre bloques de una pantalla: 18 a 24 px.

**Radios:**

| Token | Valor | Uso |
|---|---|---|
| `--radio-control` | 12 px | Campos, controles segmentados internos |
| `--radio-tarjeta` | 20 px | Tarjetas de mascota, tarjeta viva, bloques |
| `--radio-foto` | 18 px | Imágenes dentro de tarjetas |
| `--radio-hoja` | 32 px | Hojas inferiores, paneles, tableros |
| `--radio-icono` | 26 % | Ícono de app |
| `--radio-pildora` | 999 px | Botones, etiquetas de estado, buscador, barra de pestañas |

**Profundidad:**

| Token | Valor | Uso |
|---|---|---|
| `--sombra-suave` | `0 8px 24px rgba(0,0,0,.06)` | Tarjeta viva, tarjetas elevadas |
| `--sombra-flotante` | `0 10px 30px rgba(0,0,0,.12)` | Barra de pestañas, buscador, botones flotantes del mapa |
| `--sombra-hoja` | `0 -6px 30px rgba(0,0,0,.10)` | Hoja inferior sobre el mapa |
| `--vidrio` | `rgba(255,255,255,.82)` + `backdrop-filter: blur(20px) saturate(180%)` | Todo lo que flota sobre el mapa (oscuro: `rgba(28,28,30,.72)`) |

Regla: una tarjeta **o** tiene sombra **o** está sobre un fondo de otro tono, nunca ambas. Sin bordes gruesos: los bordes son de 1 px con `--color-borde`.

---

## 6. Iconografía

- Íconos de trazo, 24 px, trazo 2 px, extremos y uniones redondeados (estilo SF Symbols). Librería recomendada: **Lucide** (licencia ISC), en SVG dentro del HTML.
- Tamaños: 18 px en texto, 20 a 24 px en barras y botones, 26 px dentro del botón del micrófono.
- Color: hereda el del texto (`currentColor`). Nunca íconos multicolor.
- La huella solo aparece como relleno de imagen cuando no hay foto; no es ícono de navegación.

---

## 7. Mapa

| Aspecto | Decisión |
|---|---|
| Motor | Leaflet con teselas de OpenStreetMap |
| Estilo base | **CARTO Positron** (claro) y **CARTO Dark Matter** (oscuro): mapas grises y silenciosos para que los pines resalten. Requiere la atribución "© OpenStreetMap © CARTO" visible |
| Pines | Círculo de 30 px del color de estado, borde blanco de 3 px y punto blanco central. Seleccionado: 34 px con pulso (halo del mismo color al 10 %) |
| Avistamientos de un caso | Pines ámbar unidos al pin de la pérdida con una línea punteada de 2 px al 50 % |
| Resueltos | No se muestran por defecto. Filtro "Mostrar resueltos" en verde |
| Privacidad | Coordenadas públicas redondeadas a 3 decimales (≈ 100 m). Nunca la dirección exacta ni el teléfono en el mapa |
| Controles | Flotan en vidrio: buscador arriba y botón "Mi ubicación" abajo a la derecha, sobre la hoja |

---

## 8. Componentes

| Componente | Anatomía y reglas |
|---|---|
| **Botón primario** | Píldora Tinta con texto `--color-sobre-tinta` Geist 16/600, alto 56 px en móvil. Uno por pantalla |
| **Botón secundario** | Píldora Perla (`--color-superficie-2`) con texto Tinta |
| **Botón de micrófono** | Círculo de 60 a 64 px. Tinta en reposo, **Perdida** mientras escucha, con onda animada alrededor |
| **Etiqueta de estado** | Píldora con relleno del estado al 12 %, punto de 6 px y texto `-texto` Geist 12 a 13/500 |
| **Tarjeta de mascota** | `--radio-tarjeta`, foto de 160 × 120 (`--radio-foto`); etiqueta de estado, nombre (título 2) y "zona · hace X". Sin foto: degradado tenue del color de estado con la huella al 55 % |
| **Tarjeta viva** | Lista de campos (Reporte, Nombre, Especie, Aspecto, Zona, Contacto). Cada fila: punto de color, rótulo en `--color-texto-3` de 78 px, valor en título 2 y a la derecha ✓ verde si está completo o círculo punteado naranja con "Falta" |
| **Transcripción** | Bloque blanco con "ESCUCHANDO…" en etiqueta; las palabras que llenaron un campo van en semibold con el color de ese campo |
| **Pregunta del asistente** | Bloque Tinta con la marca de 22 px y el texto blanco 15/500. Solo una pregunta a la vez |
| **Buscador** | Píldora de vidrio con lupa, texto de ayuda y la marca a la derecha |
| **Barra de pestañas** | Píldora de vidrio flotante de 300 px: Explorar · micrófono central · Actividad |
| **Hoja inferior** | `--radio-hoja` arriba, agarradera de 36 × 5 px, `--sombra-hoja` |
| **Control segmentado** | Píldora Perla con opción activa en blanco ("Hablar / Escribir", "Mapa / Lista") |
| **Aviso (toast)** | Píldora Tinta arriba, 3 s, texto 15/500. Errores con el punto de estado Perdida |

Estados de interacción: al presionar, escala 0.97; foco visible con anillo de 2 px del acento a 2 px de distancia; deshabilitado se evita: se deja activo y se explica qué falta.

---

## 9. Movimiento

- Todo movimiento se siente físico: responde de inmediato, sigue el dedo y se asienta suavemente.
- Curva estándar `cubic-bezier(.2, .8, .2, 1)` en 240 ms; hojas y paneles con resorte (`linear()` o Motion) de 400 ms.
- La tarjeta viva ilumina el campo que acaba de llenarse: fondo del color del campo al 12 % que se desvanece en 600 ms.
- Carga: el punto encontrado recorre el anillo exterior (1,2 s por vuelta).
- Nueva pista en el detalle: el pin late una vez.
- `prefers-reduced-motion`: se reemplaza todo por fundidos de 150 ms y el punto de carga deja de girar.

---

## 10. Distribución y vistas

| Ancho | Disposición |
|---|---|
| < 768 px (celular, PWA) | Mapa a pantalla completa, hoja inferior con "Cerca de ti" y barra de pestañas flotante |
| 768–1199 px (tablet) | Dos columnas: lista de 360 px + mapa |
| ≥ 1200 px (escritorio) | Tres columnas estilo *tracker*: lista y filtros (360 px) · mapa · panel de detalle o actividad en vivo (380 px) |

Vistas del producto (todas en [`vistas.html`](vistas.html)):

| Vista | Contenido clave |
|---|---|
| Inicio | Mapa con pines por estado, buscador de vidrio, hoja "Cerca de ti" con carrusel de tarjetas, barra de pestañas |
| Nuevo reporte | Control "Hablar / Escribir", titular editorial, transcripción coloreada, tarjeta viva, pregunta del asistente, micrófono y "Revisar y publicar" |
| Detalle del caso | Foto principal, etiqueta de estado con `PF-###`, nombre en display, tres datos, pistas de la comunidad, "La vi" y "Compartir" |
| Reportar avistamiento | Tarjeta viva corta (Dónde, Cuándo, Qué viste, Contacto opcional) sobre un mini mapa |
| Publicado | Confirmación con la marca animada, identificador y acceso a compartir |

Vistas de computador (misma estructura en todas: casos a la izquierda, mapa al centro, panel contextual a la derecha):

| Vista | Contenido clave |
|---|---|
| E1 · Inicio | Casos con filtros por estado, mapa con pines y líneas punteadas hacia las pistas, panel con el caso seleccionado y actividad en vivo |
| E2 · Nuevo reporte | Hoja centrada sobre el mapa en dos columnas: voz, transcripción, pregunta y campo de texto a la izquierda; tarjeta viva, mini mapa de la zona y "Revisar y publicar" a la derecha |
| E3 · Detalle | Página completa: foto grande, datos y descripción a la izquierda; nombre en display, acciones, mapa de pistas y lista de pistas a la derecha |
| E4 · Reportar avistamiento | El mapa pasa a modo selector con el pin arrastrable; el panel derecho muestra la tarjeta viva corta y "Enviar pista" |
| E5 · Publicado | Confirmación centrada sobre el mapa con la marca animada |
| Tablet | Dos columnas (casos 340 px y mapa), sin panel derecho; el detalle abre como página |

---

## 11. PWA

- `manifest.webmanifest`: `name` "Pet Finder", `short_name` "Pet Finder", `display` "standalone", `background_color` `#F5F5F7`, `theme_color` `#F5F5F7` (oscuro: `#0B0B0C`).
- Íconos: 192 y 512 px desde `icono-app.svg`, una versión *maskable* con la marca dentro del 80 % central, y `apple-touch-icon` de 180 px.
- Respetar las zonas seguras del iPhone con `env(safe-area-inset-*)` en la barra de pestañas y la hoja.
- Sin conexión: se muestran la interfaz y el último listado, con el aviso "Sin conexión. Te mostramos lo último que vimos."

---

## 12. Accesibilidad

- Contraste AA en todo el texto (por eso cada estado tiene su variante `-texto`).
- Objetivos táctiles de mínimo 44 × 44 px.
- El color nunca es la única señal: cada estado lleva también su palabra ("Perdida", "Avistamiento"…).
- Todo lo que se dice por voz se puede escribir; el campo de texto nunca se oculta.
- Soporta modo oscuro y tamaño de letra del sistema (`rem`, no `px`, en el código).

---

## 13. Tokens listos para CSS

```css
:root {
  --color-fondo:#F5F5F7; --color-superficie:#FFFFFF; --color-superficie-2:#F5F5F7;
  --color-tinta:#0B0B0C; --color-texto-2:#6E6E73; --color-texto-3:#86868B;
  --color-borde:rgba(0,0,0,.08); --color-acento:#0071E3; --color-sobre-tinta:#FFFFFF;
  --estado-perdida:#FF5F1F; --estado-perdida-texto:#C2410C;
  --estado-avistamiento:#F5A300; --estado-avistamiento-texto:#8A5A00;
  --estado-encontrada:#7A5AF8; --estado-encontrada-texto:#5B3FD6;
  --estado-resuelto:#1DB954; --estado-resuelto-texto:#0F7A36;
  --fuente-ui:'Geist',system-ui,sans-serif; --fuente-editorial:'Instrument Serif',Georgia,serif;
  --fuente-mono:'Geist Mono',ui-monospace,monospace;
  --radio-control:12px; --radio-tarjeta:20px; --radio-foto:18px; --radio-hoja:32px; --radio-pildora:999px;
  --sombra-suave:0 8px 24px rgba(0,0,0,.06); --sombra-flotante:0 10px 30px rgba(0,0,0,.12);
  --sombra-hoja:0 -6px 30px rgba(0,0,0,.10);
  --vidrio:rgba(255,255,255,.82); --curva:cubic-bezier(.2,.8,.2,1); --duracion:240ms;
}
@media (prefers-color-scheme: dark) {
  :root {
    --color-fondo:#0B0B0C; --color-superficie:#1C1C1E; --color-superficie-2:#2C2C2E;
    --color-tinta:#F5F5F7; --color-texto-2:#A1A1A6; --color-texto-3:#8E8E93;
    --color-borde:rgba(255,255,255,.10); --color-acento:#0A84FF; --color-sobre-tinta:#0B0B0C;
    --estado-perdida:#FF7A45; --estado-perdida-texto:#FF9A70;
    --estado-avistamiento:#FFB31A; --estado-avistamiento-texto:#FFC94D;
    --estado-encontrada:#9580FF; --estado-encontrada-texto:#B3A3FF;
    --estado-resuelto:#30D158; --estado-resuelto-texto:#5EE082;
    --vidrio:rgba(28,28,30,.72);
  }
}
```

---

## 14. Qué nunca hacer

- Más de un botón primario por pantalla, o un segundo color de acento.
- Colores de estado como decoración.
- Hexadecimales sueltos en el CSS de un componente.
- Bordes gruesos, sombras duras, degradados llamativos o neón.
- Emojis en la interfaz.
- Mostrar teléfono, correo o dirección exacta en listas o en el mapa.
- Publicar algo que la persona no confirmó.
- Redibujar el logo, cambiar sus proporciones o ponerle efectos.

## 15. Lista de revisión para cualquier cambio visual

- [ ] Usa solo tokens de §13; no hay hexadecimales nuevos.
- [ ] Un solo botón primario en la pantalla.
- [ ] Funciona a 375 px y a 1440 px sin desplazamiento horizontal.
- [ ] Se ve bien en modo claro y oscuro.
- [ ] Textos en español, con la voz de §2.3.
- [ ] Contraste AA y objetivos táctiles de 44 px.
- [ ] Coincide con la vista correspondiente de `vistas.html`; si la cambia, se actualiza también `vistas.html`.
