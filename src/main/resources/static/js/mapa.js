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

export function hayMapa() {
  return typeof window.L !== 'undefined';
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
  return caso.tipo === 'ENCONTRADA' ? 'encontrada' : 'perdida';
}

function tieneCoordenadas(elemento) {
  return typeof elemento.latitud === 'number' && typeof elemento.longitud === 'number';
}

/**
 * Mapa principal: un pin por caso con coordenadas. Los pines se reutilizan entre
 * actualizaciones para que el sondeo periódico no los haga parpadear.
 */
export function crearMapa(contenedor, { alElegirCaso = () => {}, conZoom = true } = {}) {
  const mapa = window.L.map(contenedor, { zoomControl: false, attributionControl: true })
    .setView(CENTRO_INICIAL, ZOOM_INICIAL);
  if (conZoom) window.L.control.zoom({ position: 'bottomright' }).addTo(mapa);
  const soltarTeselas = conTeselas(mapa);
  const soltarTamano = ajustarAlTamano(mapa, contenedor);

  const pines = new Map();
  const capaPistas = window.L.layerGroup().addTo(mapa);
  let seleccionado = null;
  let yaEncuadro = false;

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
    mapa.fitBounds(limites, { padding: [60, 60], maxZoom: 15 });
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
    mapa.fitBounds(window.L.latLngBounds(puntos), { padding: [80, 80], maxZoom: 16 });
  }

  function limpiarPistas() {
    capaPistas.clearLayers();
  }

  return {
    pintarCasos,
    mostrarCaso,
    limpiarPistas,
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
 * Mapa pequeño con un solo pin que la persona coloca tocando o arrastra. Se usa
 * para decir dónde se perdió una mascota o dónde se la vio: sin este gesto un
 * reporte publicado por voz no tendría coordenadas y no aparecería en el mapa.
 */
export function crearSelector(contenedor, { estado = 'perdida', alElegir = () => {}, vistaEn = null } = {}) {
  const mapa = window.L.map(contenedor, { zoomControl: false, attributionControl: true })
    .setView(vistaEn || CENTRO_INICIAL, vistaEn ? 15 : ZOOM_INICIAL);
  const soltarTeselas = conTeselas(mapa);
  const soltarTamano = ajustarAlTamano(mapa, contenedor);
  let pin = null;

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
    invalidar: () => mapa.invalidateSize(),
    destruir: () => {
      soltarTeselas();
      soltarTamano();
      mapa.remove();
    },
  };
}
