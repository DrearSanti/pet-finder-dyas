// Cliente de la API REST de Pet Finder: la única puerta de la página al servidor.
// Cada función corresponde a una ruta del adaptador web y devuelve el JSON ya parseado.
// Nunca muestra nada: quien llama decide cómo avisar (zona data-prueba="mensaje").

export class ErrorApi extends Error {
  constructor(status, mensaje) { super(mensaje); this.status = status; }
}
// Toda llamada no 2xx termina aquí: el mensaje es el campo `error` del servidor.
async function pedir(metodo, ruta, cuerpo) {
  const r = await fetch(ruta, {
    method: metodo,
    headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  });
  if (r.status === 204) return null;
  const datos = await r.json().catch(() => ({}));
  if (!r.ok) throw new ErrorApi(r.status, datos.error || 'No pudimos completar la operación.');
  return datos;
}

// El id viaja en la ruta: se codifica para que un texto raro no cambie la URL.
function rutaReporte(id) {
  return `/api/reportes/${encodeURIComponent(id)}`;
}

/** Casos activos en forma de resumen (contacto enmascarado), el más reciente primero. */
export async function listarReportes() {
  return pedir('GET', '/api/reportes');
}

/** Detalle de un caso, con contacto completo y avistamientos. 404 si no existe. */
export async function consultarReporte(id) {
  return pedir('GET', rutaReporte(id));
}

/** Publica un reporte: solo se llama cuando la persona toca "Publicar reporte". */
export async function crearReporte(solicitud) {
  return pedir('POST', '/api/reportes', solicitud);
}

/** Agrega una pista a un caso y devuelve el detalle ya actualizado. */
export async function registrarAvistamiento(id, avistamiento) {
  return pedir('POST', `${rutaReporte(id)}/avistamientos`, avistamiento);
}

/** Marca el caso como resuelto (204, sin cuerpo). 409 si ya no está activo. */
export async function resolverReporte(id) {
  return pedir('POST', `${rutaReporte(id)}/resolver`);
}

/** Cierra el caso sin resolverlo (204, sin cuerpo). 409 si ya no está activo. */
export async function cerrarReporte(id) {
  return pedir('POST', `${rutaReporte(id)}/cerrar`);
}

/** Envía la frase transcrita (nunca audio) al procesador de comandos de voz. */
export async function enviarVoz(texto, nombreContacto, medioContacto) {
  return pedir('POST', '/api/voz', { texto, nombreContacto, medioContacto });
}

/**
 * Un turno del asistente: manda el borrador actual (null en el primer turno) y la frase nueva.
 * El asistente solo completa el borrador; publicar sigue siendo crearReporte().
 */
export async function turnoAsistente(borrador, texto) {
  return pedir('POST', '/api/asistente/turno', { borrador: borrador ?? null, texto });
}
