// Mapa con Leaflet 1.9.4 (global `L`, cargado desde cdnjs en index.html).
// La página no depende de él: si la CDN no responde, hayMapa() es falso y la lista sigue funcionando.
//
// Teselas: CARTO Positron y Dark Matter (mapas grises y silenciosos, DESIGN.md §7). Desde 2026 CARTO exige
// una clave gratuita (sin ella devuelve una imagen "API KEY REQUIRED"). La clave vive en js/config-local.js,
// un archivo que Git ignora, y llega aquí como window.PETFINDER_CLAVE_CARTO. Sin ella, el mapa usa
// OpenStreetMap y se deja gris con CSS, para que la app siempre muestre un mapa real.

const ATRIBUCION_CARTO = '© OpenStreetMap © CARTO';
const ATRIBUCION_OSM = '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
const CLAVE_CARTO = String(window.PETFINDER_CLAVE_CARTO || '').trim();
const TESELAS_CARTO = {
  claro: 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png',
  oscuro: 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',
};
const TESELAS_OSM = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
// Bogotá: el centro cuando aún no hay casos con coordenadas.
const CENTRO_INICIAL = [4.711, -74.0721];
const ZOOM_INICIAL = 12;
// La flecha de "Mi ubicación" de vistas.html. Es un SVG fijo del código, nunca datos.
const ICONO_UBICACION = '<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M3 11l18-8-8 18-2-8-8-2z"/></svg>';
const MENSAJES_UBICACION = {
  1: 'No tenemos permiso para ver tu ubicación. Actívalo en la configuración de tu navegador.',
  2: 'No pudimos saber dónde estás. Intenta de nuevo en un momento.',
  3: 'Tu ubicación tardó demasiado. Intenta de nuevo en un momento.',
};

export function hayMapa() {
  return typeof window.L !== 'undefined';
}

/**
 * La posición de la persona. Se queda en el navegador: filtrar "Cerca de ti" no la envía al servidor;
 * solo viaja si la persona la usa como el pin de un reporte. Como la voz, necesita https o localhost.
 */
export function obtenerUbicacion() {
  return new Promise((resolver, rechazar) => {
    if (!('geolocation' in navigator)) {
      rechazar(new Error('Tu navegador no comparte la ubicación. Busca tu barrio en el campo de búsqueda.'));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (posicion) => resolver({ latitud: posicion.coords.latitude, longitud: posicion.coords.longitude }),
      (error) => rechazar(new Error(MENSAJES_UBICACION[error.code] || MENSAJES_UBICACION[2])),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 },
    );
  });
}

/** Distancia en km entre dos puntos {latitud, longitud} (Haversine): basta para decir "a 1,2 km". */
export function distanciaKm(a, b) {
  const radianes = (grados) => (grados * Math.PI) / 180;
  const dLatitud = radianes(b.latitud - a.latitud);
  const dLongitud = radianes(b.longitud - a.longitud);
  const h = Math.sin(dLatitud / 2) ** 2
    + Math.cos(radianes(a.latitud)) * Math.cos(radianes(b.latitud)) * Math.sin(dLongitud / 2) ** 2;
  return 2 * 6371 * Math.asin(Math.sqrt(h));
}

/** El botón redondo de vidrio de "Mi ubicación", como control de Leaflet abajo a la derecha (DESIGN.md §7). */
function agregarBotonUbicacion(mapa, etiqueta, alTocar) {
  const Control = window.L.Control.extend({
    onAdd() {
      const boton = window.L.DomUtil.create('button', 'boton-ubicacion vidrio');
      boton.type = 'button';
      boton.title = etiqueta;
      boton.setAttribute('aria-label', etiqueta);
      boton.insertAdjacentHTML('beforeend', ICONO_UBICACION);
      // Sin esto, el toque también llega al mapa y en el selector movería el pin.
      window.L.DomEvent.disableClickPropagation(boton);
      window.L.DomEvent.on(boton, 'click', () => alTocar(boton));
      return boton;
    },
  });
  return new Control({ position: 'bottomright' }).addTo(mapa).getContainer();
}

/** "Estás aquí": un punto del color de acento, distinto de los pines de estado. */
function iconoYo() {
  return window.L.divIcon({ className: 'pin-yo', iconSize: [18, 18], iconAnchor: [9, 9] });
}

/** Sigue el tema del sistema, o el que fije data-tema en <html> (mismo criterio que tokens.css). */
function esOscuro() {
  const fijo = document.documentElement.dataset.tema;
  if (fijo) return fijo === 'oscuro';
  return window.matchMedia('(prefers-color-scheme: dark)').matches;
}

/** El color de cada estado sale de los tokens: el mapa no define ninguno propio. */
function colorDeToken(token) {
  return getComputedStyle(document.documentElement).getPropertyValue(token).trim();
}

/** Pone la capa de teselas del tema actual y la cambia sola cuando el tema cambia. */
function conTeselas(mapa) {
  let capa = null;
  // La clase le dice al CSS que estas teselas son de OpenStreetMap y hay que dejarlas grises.
  mapa.getContainer().classList.toggle('teselas-osm', !CLAVE_CARTO);
  const poner = () => {
    if (capa) capa.remove();
    const tema = esOscuro() ? 'oscuro' : 'claro';
    capa = CLAVE_CARTO
      ? window.L.tileLayer(`${TESELAS_CARTO[tema]}?key=${encodeURIComponent(CLAVE_CARTO)}`, {
        attribution: ATRIBUCION_CARTO, subdomains: 'abcd', maxZoom: 19,
      })
      : window.L.tileLayer(TESELAS_OSM, { attribution: ATRIBUCION_OSM, maxZoom: 19 });
    capa.addTo(mapa);
  };
  poner();
  const consulta = window.matchMedia('(prefers-color-scheme: dark)');
  const alCambiar = () => poner();
  consulta.addEventListener('change', alCambiar);
  const observador = new MutationObserver(alCambiar);
  observador.observe(document.documentElement, { attributes: true, attributeFilter: ['data-tema'] });
  return () => {
    consulta.removeEventListener('change', alCambiar);
    observador.disconnect();
  };
}

/** Leaflet mide su contenedor una sola vez; esto lo vuelve a medir cada vez que el diseño lo cambia de tamaño. */
function ajustarAlTamano(mapa, contenedor) {
  const observador = new ResizeObserver(() => mapa.invalidateSize());
  observador.observe(contenedor);
  return () => observador.disconnect();
}

/** Un pin es un div con las clases de app.css: el círculo de 30 px del color del estado. */
function icono(estado, seleccionado) {
  const tamano = seleccionado ? 36 : 30;
  return window.L.divIcon({
    className: `pin ${estado}${seleccionado ? ' sel' : ''}`,
    iconSize: [tamano, tamano],
    iconAnchor: [tamano / 2, tamano / 2],
  });
}

function estadoDePin(caso) {
  // Una pérdida que alguien dice tener ya se muestra como encontrada, aunque siga activa.
  return caso.tipo === 'ENCONTRADA' || caso.laTieneAlguien ? 'encontrada' : 'perdida';
}

function tieneCoordenadas(elemento) {
  return typeof elemento.latitud === 'number' && typeof elemento.longitud === 'number';
}

/**
 * Mapa principal: un pin por caso con coordenadas. Los pines se reutilizan entre
 * actualizaciones para que el sondeo periódico no los haga parpadear.
 */
export function crearMapa(contenedor, {
  alElegirCaso = () => {}, conZoom = true, alTocarUbicacion = null, altoTapado = () => 0,
} = {}) {
  const mapa = window.L.map(contenedor, { zoomControl: false, attributionControl: true })
    .setView(CENTRO_INICIAL, ZOOM_INICIAL);
  if (conZoom) window.L.control.zoom({ position: 'bottomright' }).addTo(mapa);
  // Después del zoom para que quede encima de él: Leaflet apila hacia arriba en las esquinas de abajo.
  const botonUbicacion = alTocarUbicacion
    ? agregarBotonUbicacion(mapa, 'Casos cerca de ti', alTocarUbicacion)
    : null;
  const soltarTeselas = conTeselas(mapa);
  const soltarTamano = ajustarAlTamano(mapa, contenedor);

  const pines = new Map();
  const capaPistas = window.L.layerGroup().addTo(mapa);
  let seleccionado = null;
  let yaEncuadro = false;
  let pinYo = null;

  /**
   * Márgenes para encuadrar. En el celular la hoja tapa la parte de abajo del mapa (altoTapado la mide):
   * sin ese margen, lo que se quiere mostrar puede quedar debajo de la hoja.
   */
  function margenes(borde) {
    return { paddingTopLeft: [borde, borde], paddingBottomRight: [borde, borde + altoTapado()] };
  }

  /** Centra un solo punto en la parte visible del mapa, por encima de la hoja. */
  function centrarVisible(punto, zoom) {
    mapa.setView(punto, zoom, { animate: false });
    const tapado = altoTapado();
    if (tapado > 0) mapa.panBy([0, tapado / 2], { animate: false });
  }

  function pintarCasos(casos, idSeleccionado = null) {
    seleccionado = idSeleccionado;
    const vigentes = new Set();
    for (const caso of casos.filter(tieneCoordenadas)) {
      vigentes.add(caso.id);
      const posicion = [caso.latitud, caso.longitud];
      const esSel = caso.id === idSeleccionado;
      let pin = pines.get(caso.id);
      if (!pin) {
        pin = window.L.marker(posicion, { icon: icono(estadoDePin(caso), esSel), title: caso.nombreMascota || caso.zona });
        pin.on('click', () => alElegirCaso(caso.id));
        pin.addTo(mapa);
        pines.set(caso.id, pin);
      } else {
        pin.setLatLng(posicion);
        pin.setIcon(icono(estadoDePin(caso), esSel));
      }
      pin.setZIndexOffset(esSel ? 1000 : 0);
    }
    for (const [id, pin] of pines) {
      if (!vigentes.has(id)) {
        pin.remove();
        pines.delete(id);
      }
    }
    if (!yaEncuadro && vigentes.size > 0) {
      encuadrar(casos.filter(tieneCoordenadas));
      yaEncuadro = true;
    }
  }

  function encuadrar(conCoordenadas) {
    if (conCoordenadas.length === 0) return;
    const limites = window.L.latLngBounds(conCoordenadas.map((c) => [c.latitud, c.longitud]));
    mapa.fitBounds(limites, { ...margenes(60), maxZoom: 15 });
  }

  /** Centra un caso y dibuja sus avistamientos como pines ámbar unidos por una línea punteada. */
  function mostrarCaso(caso) {
    capaPistas.clearLayers();
    if (!tieneCoordenadas(caso)) return;
    const origen = [caso.latitud, caso.longitud];
    const puntos = [origen];
    for (const pista of caso.avistamientos || []) {
      if (!tieneCoordenadas(pista)) continue;
      const destino = [pista.latitud, pista.longitud];
      puntos.push(destino);
      window.L.polyline([origen, destino], {
        color: colorDeToken('--estado-avistamiento'), weight: 2, opacity: 0.5, dashArray: '6 6',
      }).addTo(capaPistas);
      window.L.marker(destino, { icon: icono('avistamiento', false), title: 'Avistamiento' }).addTo(capaPistas);
    }
    mapa.fitBounds(window.L.latLngBounds(puntos), { ...margenes(80), maxZoom: 16 });
  }

  function limpiarPistas() {
    capaPistas.clearLayers();
  }

  /** Pone el punto "Estás aquí" y encuadra la posición junto con los casos cercanos. */
  function mostrarUbicacion(posicion, cercanos = []) {
    const punto = [posicion.latitud, posicion.longitud];
    if (!pinYo) {
      pinYo = window.L.marker(punto, { icon: iconoYo(), title: 'Estás aquí', interactive: false, zIndexOffset: 2000 })
        .addTo(mapa);
    } else {
      pinYo.setLatLng(punto);
    }
    const puntos = [punto, ...cercanos.filter(tieneCoordenadas).map((c) => [c.latitud, c.longitud])];
    if (puntos.length > 1) mapa.fitBounds(window.L.latLngBounds(puntos), { ...margenes(60), maxZoom: 15 });
    else centrarVisible(punto, 14);
  }

  function quitarUbicacion() {
    if (pinYo) {
      pinYo.remove();
      pinYo = null;
    }
  }

  return {
    pintarCasos,
    mostrarCaso,
    limpiarPistas,
    mostrarUbicacion,
    quitarUbicacion,
    botonUbicacion: () => botonUbicacion,
    encuadrarTodo: (casos) => encuadrar(casos.filter(tieneCoordenadas)),
    /** Leaflet mide su contenedor al crearse: si estaba oculto hay que avisarle cuando aparece. */
    invalidar: () => mapa.invalidateSize(),
    destruir: () => {
      soltarTeselas();
      soltarTamano();
      mapa.remove();
    },
    idSeleccionado: () => seleccionado,
  };
}

/**
 * Mapa pequeño con un solo pin, sugerido automáticamente o elegido por la persona.
 * Solo los gestos explícitos llaman a alElegir: así una sugerencia nunca bloquea
 * los siguientes movimientos automáticos como si la persona ya hubiera confirmado.
 */
export function crearSelector(contenedor, {
  estado = 'perdida', alElegir = () => {}, vistaEn = null, alFallarUbicacion = () => {},
} = {}) {
  const mapa = window.L.map(contenedor, { zoomControl: false, attributionControl: true })
    .setView(vistaEn || CENTRO_INICIAL, vistaEn ? 15 : ZOOM_INICIAL);
  const soltarTeselas = conTeselas(mapa);
  const soltarTamano = ajustarAlTamano(mapa, contenedor);
  let pin = null;
  // La ubicación tarda: si la persona sale de la vista antes de que llegue, el mapa ya no existe.
  let vivo = true;

  /** "Mi ubicación": el pin cae donde está la persona, que solo lo ajusta si hace falta. */
  agregarBotonUbicacion(mapa, 'Usar mi ubicación', async (boton) => {
    boton.setAttribute('aria-busy', 'true');
    try {
      const { latitud, longitud } = await obtenerUbicacion();
      if (!vivo) return;
      colocar(latitud, longitud);
      mapa.setView([latitud, longitud], 16);
      alElegir(latitud, longitud);
    } catch (error) {
      if (vivo) alFallarUbicacion(error.message);
    } finally {
      boton.removeAttribute('aria-busy');
    }
  });

  function colocar(latitud, longitud) {
    const posicion = [latitud, longitud];
    if (!pin) {
      pin = window.L.marker(posicion, { icon: icono(estado, true), draggable: true, title: 'Arrastra el pin si hace falta' })
        .addTo(mapa);
      pin.on('dragend', () => {
        const { lat, lng } = pin.getLatLng();
        alElegir(lat, lng);
      });
    } else {
      pin.setLatLng(posicion);
    }
  }

  mapa.on('click', (evento) => {
    colocar(evento.latlng.lat, evento.latlng.lng);
    alElegir(evento.latlng.lat, evento.latlng.lng);
  });

  return {
    colocar,
    mover: (latitud, longitud) => {
      colocar(latitud, longitud);
      mapa.setView([latitud, longitud], 16);
    },
    invalidar: () => mapa.invalidateSize(),
    destruir: () => {
      vivo = false;
      soltarTeselas();
      soltarTamano();
      mapa.remove();
    },
  };
}
