import http from 'k6/http';
import { check } from 'k6';

// Prueba corta del pipeline: el mismo SLO y las mismas operaciones de carga.js, en un minuto y medio.
// Corre después de cada despliegue contra la URL desplegada; si el SLO no se cumple, k6 sale con un
// código distinto de 0 y el despliegue queda en rojo.
//
// Modelo abierto: la tasa de llegada es fija (11 iteraciones de 3 peticiones por segundo, unas 33 req/s).
// Si el sistema no da abasto, las iteraciones se acumulan o se pierden y fallan los umbrales de
// throughput e iteraciones perdidas; el resultado no depende de cuántos usuarios virtuales se elijan.
//
// Cada caso que crea lo cierra al final del ciclo, para no dejar reportes de prueba en el mapa.
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const CABECERAS = { 'Content-Type': 'application/json' };
const TASA = Number(__ENV.ITERACIONES_POR_SEGUNDO || 11);
const CALENTAMIENTO = __ENV.CALENTAMIENTO || '30s';

export const options = {
  scenarios: {
    // El calentamiento no cuenta para el SLO: despierta el servicio si estaba dormido y deja que la JVM compile.
    calentamiento: {
      executor: 'constant-arrival-rate', rate: Math.ceil(TASA / 2), timeUnit: '1s',
      duration: CALENTAMIENTO, preAllocatedVUs: 10, maxVUs: 40,
    },
    medicion: {
      executor: 'constant-arrival-rate', rate: TASA, timeUnit: '1s',
      duration: __ENV.MEDICION || '60s', startTime: CALENTAMIENTO,
      preAllocatedVUs: 20, maxVUs: 60,
    },
  },
  thresholds: {
    'http_req_duration{scenario:medicion}': ['p(95)<500'],
    'http_req_failed{scenario:medicion}': ['rate<0.01'],
    // 30 req/s del SLO durante los 60 s de medición
    'http_reqs{scenario:medicion}': ['count>=1800'],
    // Por operación, como en carga.js: sin esto el resumen no separa las latencias
    'http_req_duration{scenario:medicion,name:voz_registrar}': ['p(95)<500'],
    'http_req_duration{scenario:medicion,name:voz_listar}': ['p(95)<500'],
    'http_req_duration{scenario:medicion,name:limpieza}': ['p(95)<500'],
    'dropped_iterations{scenario:medicion}': ['count==0'],
  },
};

export default function () {
  // El intérprete de voz solo acepta letras en el nombre: el contador va en la zona para que cada reporte sea distinto.
  const registro = http.post(
    `${BASE}/api/voz`,
    JSON.stringify({
      texto: `perdí un perro llamado Max en Chía ${__VU}-${__ITER}`,
      nombreContacto: 'Prueba de carga',
      medioContacto: '3001112233',
    }),
    { headers: CABECERAS, tags: { name: 'voz_registrar' } },
  );
  check(registro, { 'voz_registrar responde 201': (r) => r.status === 201 });

  const listado = http.post(`${BASE}/api/voz`, JSON.stringify({ texto: 'ver reportes' }), {
    headers: CABECERAS, tags: { name: 'voz_listar' },
  });
  check(listado, { 'voz_listar responde 200': (r) => r.status === 200 });

  // Limpieza: la tercera petición del ciclo. También cuenta para el SLO.
  if (registro.status === 201) {
    const id = registro.json('reportes.0.id');
    const cierre = http.post(`${BASE}/api/reportes/${id}/cerrar`, null, { tags: { name: 'limpieza' } });
    check(cierre, { 'limpieza responde 204': (r) => r.status === 204 });
  }
}
