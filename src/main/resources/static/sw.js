// Service worker de Pet Finder: la interfaz abre aunque no haya internet; los datos, nunca desde caché.

const VERSION = 'pet-finder-v1';
const MENSAJE_SIN_CONEXION = 'Sin conexión. Te mostramos lo último que vimos.';

// La interfaz completa. Se precachea al instalar para que abra sin red desde la primera visita.
const INTERFAZ = [
  '/',
  '/index.html',
  '/manifest.webmanifest',
  '/css/tokens.css',
  '/css/app.css',
  '/js/app.js',
  '/js/api.js',
  '/js/voz.js',
  '/js/tarjeta-viva.js',
  '/js/mapa.js',
  '/img/marca.svg',
  '/img/icono-180.png',
  '/img/icono-192.png',
  '/img/icono-512.png',
  '/img/icono-512-maskable.png',
];

self.addEventListener('install', (evento) => {
  evento.waitUntil((async () => {
    const cache = await caches.open(VERSION);
    // Uno por uno: si un archivo falla, los demás se guardan igual y la instalación no se cae.
    await Promise.all(INTERFAZ.map((ruta) => cache.add(ruta).catch(() => {})));
    await self.skipWaiting();
  })());
});

self.addEventListener('activate', (evento) => {
  evento.waitUntil((async () => {
    for (const nombre of await caches.keys()) {
      if (nombre !== VERSION) await caches.delete(nombre);
    }
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', (evento) => {
  const peticion = evento.request;
  if (peticion.method !== 'GET') return;
  const url = new URL(peticion.url);
  // Mapa, fuentes y Leaflet viven en otros dominios: el navegador se encarga de ellos.
  if (url.origin !== self.location.origin) return;

  // Los datos nunca se guardan en caché: un caso resuelto no puede seguir apareciendo como activo.
  // Si no hay red, se responde con el mismo formato de error de la API, para que la página lo muestre.
  if (url.pathname.startsWith('/api/')) {
    evento.respondWith(fetch(peticion).catch(() => new Response(
      JSON.stringify({ error: MENSAJE_SIN_CONEXION }),
      { status: 503, headers: { 'Content-Type': 'application/json' } },
    )));
    return;
  }

  // La interfaz: primero la red, así una versión nueva se ve al instante; sin red, la copia guardada.
  evento.respondWith((async () => {
    try {
      const respuesta = await fetch(peticion);
      if (respuesta.ok) {
        const cache = await caches.open(VERSION);
        cache.put(peticion, respuesta.clone());
      }
      return respuesta;
    } catch (error) {
      const guardada = await caches.match(peticion);
      if (guardada) return guardada;
      if (peticion.mode === 'navigate') {
        const inicio = await caches.match('/');
        if (inicio) return inicio;
      }
      throw error;
    }
  })());
});
