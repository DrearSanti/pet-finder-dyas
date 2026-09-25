import http from 'k6/http';
import { check, sleep } from 'k6';

// Baseline: el mismo escenario de carga.js con 5 usuarios, para tener la latencia de referencia sin presión.
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const CABECERAS = { 'Content-Type': 'application/json' };

export const options = {
  vus: 5,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
    // Umbrales por etiqueta: sin ellos el resumen de k6 no separa las dos operaciones.
    'http_req_duration{name:voz_registrar}': ['p(95)<500'],
    'http_req_duration{name:voz_listar}': ['p(95)<500'],
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

  const listado = http.post(
    `${BASE}/api/voz`,
    JSON.stringify({ texto: 'ver reportes' }),
    { headers: CABECERAS, tags: { name: 'voz_listar' } },
  );
  check(listado, { 'voz_listar responde 200': (r) => r.status === 200 });

  sleep(1);
}
