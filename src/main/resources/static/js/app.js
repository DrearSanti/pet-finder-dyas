// Pet Finder en el navegador: enruta por hash y conecta la lista, el mapa, la tarjeta viva y la voz.
// Solo habla con el servidor a través de api.js. Todo texto que viene del servidor o de la persona
// se inserta con textContent: nunca con innerHTML.

import {
  ErrorApi, listarReportes, consultarReporte, registrarAvistamiento,
} from './api.js';
import { crearTarjetaViva } from './tarjeta-viva.js';
import { hayMapa, crearMapa, crearSelector } from './mapa.js';
import {
  soportaVoz, iniciarCaptura, detenerCaptura, capturando,
} from './voz.js';

const CADA_CUANTO_ACTUALIZAR = 15000;

const aplicacion = document.querySelector('.app');
const porPrueba = (nombre) => document.querySelector(`[data-prueba="${nombre}"]`);
const porRol = (rol) => document.querySelector(`[data-rol="${rol}"]`);
const panel = (nombre) => document.querySelector(`[data-vista-panel="${nombre}"]`);

const ETIQUETA_TIPO = { PERDIDA: 'Perdida', ENCONTRADA: 'Encontrada' };
const CLASE_TIPO = { PERDIDA: 'perdida', ENCONTRADA: 'encontrada' };
const PATA = '<svg viewBox="0 0 200 200" fill="currentColor" aria-hidden="true"><circle cx="44" cy="92" r="17"/>'
  + '<circle cx="76" cy="56" r="19"/><circle cx="124" cy="56" r="19"/><circle cx="156" cy="92" r="17"/>'
  + '<ellipse cx="100" cy="138" rx="44" ry="36"/></svg>';

let casos = [];
let firmaCasos = '';
let filtro = 'todos';
let busqueda = '';
let idSeleccionado = null;
let ultimoPublicado = null;
let rutaEnCurso = 0;
let mapa = null;
let selector = null;
let microfonoActivo = null;

/* ---------- Utilidades ---------- */

function el(etiqueta, clase, texto) {
  const nodo = document.createElement(etiqueta);
  if (clase) nodo.className = clase;
  if (texto !== undefined) nodo.textContent = texto;
  return nodo;
}

function vacio(valor) {
  return valor === null || valor === undefined || String(valor).trim() === '';
}

function haceCuanto(iso) {
  const minutos = Math.floor((Date.now() - new Date(iso).getTime()) / 60000);
  if (!(minutos >= 1)) return 'hace un momento';
  if (minutos < 60) return `hace ${minutos} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `hace ${horas} h`;
  const dias = Math.floor(horas / 24);
  return dias === 1 ? 'ayer' : `hace ${dias} días`;
}

/** El título del caso: el nombre de la mascota, o para un hallazgo lo primero que dice su descripción. */
function tituloDe(caso) {
  if (!vacio(caso.nombreMascota)) return caso.nombreMascota;
  return (caso.descripcionMascota || 'Animal encontrado').split(',')[0].trim();
}

function aspectoDe(caso) {
  if (caso.tipo === 'ENCONTRADA') return caso.descripcionMascota || '';
  return [caso.especie, caso.raza, caso.color, caso.senas].filter((v) => !vacio(v)).join(' · ');
}

/** Un aviso arriba, 3 s. Los errores se quedan más tiempo porque traen qué hacer. Nunca alert(). */
function mostrarMensaje(texto, esError = false) {
  const aviso = el('div', `aviso${esError ? ' error' : ''}`, texto);
  porPrueba('mensaje').append(aviso);
  setTimeout(() => aviso.remove(), esError ? 6000 : 3000);
}

function mostrarError(error) {
  if (error instanceof ErrorApi) {
    mostrarMensaje(error.message, true);
  } else {
    mostrarMensaje('No pudimos conectar con el servidor. Revisa tu internet e intenta de nuevo.', true);
  }
}

function navegar(ruta) {
  if (location.hash === ruta) enrutar();
  else location.hash = ruta;
}

async function compartir(titulo, ruta) {
  const url = new URL(ruta, location.href).href;
  try {
    if (navigator.share) {
      await navigator.share({ title: titulo, url });
    } else {
      await navigator.clipboard.writeText(url);
      mostrarMensaje('Enlace copiado');
    }
  } catch (error) {
    if (error.name !== 'AbortError') mostrarMensaje('No pudimos compartir. Copia el enlace de la barra de direcciones.', true);
  }
}

/* ---------- Lista de casos y mapa ---------- */

function casosFiltrados() {
  const texto = busqueda.trim().toLowerCase();
  return casos.filter((caso) => {
    if (filtro !== 'todos' && caso.tipo !== filtro) return false;
    if (!texto) return true;
    return [tituloDe(caso), caso.zona, caso.especie, caso.descripcionMascota]
      .some((campo) => !vacio(campo) && String(campo).toLowerCase().includes(texto));
  });
}

function crearTarjetaCaso(caso) {
  const enlace = el('a', `caso${caso.id === idSeleccionado ? ' on' : ''}`);
  enlace.href = `#/caso/${encodeURIComponent(caso.id)}`;
  enlace.dataset.prueba = 'tarjeta-caso';
  enlace.dataset.id = caso.id;
  const clase = CLASE_TIPO[caso.tipo] || 'perdida';
  const foto = el('div', `mini foto ${clase}`);
  foto.insertAdjacentHTML('beforeend', PATA);
  const pistas = caso.cantidadAvistamientos > 0
    ? ` · ${caso.cantidadAvistamientos} ${caso.cantidadAvistamientos === 1 ? 'pista' : 'pistas'}`
    : '';
  const texto = el('div', 't');
  texto.append(
    el('span', 'nombre', tituloDe(caso)),
    el('span', 'meta', `${caso.zona} · ${haceCuanto(caso.fechaCreacion)}${pistas}`),
  );
  enlace.append(foto, texto, el('span', `estado ${clase}`, ETIQUETA_TIPO[caso.tipo] || 'Caso'));
  return enlace;
}

function pintarLista() {
  const lista = porPrueba('lista-casos');
  const visibles = casosFiltrados();
  porRol('cuenta').textContent = `${visibles.length} ${visibles.length === 1 ? 'activo' : 'activos'}`;
  if (visibles.length === 0) {
    lista.replaceChildren(el('p', 'vacio', casos.length === 0
      ? 'Aún no hay casos cerca. Si viste una mascota, cuéntanos.'
      : 'Ningún caso coincide con tu búsqueda.'));
  } else {
    lista.replaceChildren(...visibles.map(crearTarjetaCaso));
  }
  if (mapa) mapa.pintarCasos(visibles, idSeleccionado);
}

/** Vuelve a pedir los casos; solo repinta si algo cambió, para que el sondeo no haga parpadear nada. */
async function cargarCasos(forzar = false) {
  try {
    const recibidos = await listarReportes();
    const firma = JSON.stringify(recibidos);
    if (!forzar && firma === firmaCasos) return;
    firmaCasos = firma;
    casos = recibidos;
    pintarLista();
  } catch (error) {
    if (casos.length === 0) mostrarError(error);
  }
}

function iniciarMapa() {
  if (!hayMapa()) {
    porRol('mapa-aviso').hidden = false;
    return;
  }
  mapa = crearMapa(porPrueba('mapa'), {
    alElegirCaso: (id) => navegar(`#/caso/${encodeURIComponent(id)}`),
    conZoom: window.matchMedia('(min-width: 768px)').matches,
  });
}

/* ---------- Voz ---------- */

let vigilante = null;

function actualizarMicrofonos() {
  const escuchando = capturando();
  for (const boton of document.querySelectorAll('[data-prueba^="boton-microfono"]')) {
    boton.classList.toggle('escuchando', escuchando && boton === microfonoActivo);
    boton.setAttribute('aria-label', escuchando && boton === microfonoActivo ? 'Dejar de escuchar' : boton.dataset.etiqueta);
  }
  porRol('estado-voz').textContent = escuchando && microfonoActivo === porPrueba('boton-microfono')
    ? 'Escuchando…' : 'Di o escribe lo que pasó';
}

/** voz.js no avisa cuando termina sin texto ni error; se vigila para que el botón no quede "escuchando". */
function vigilarCaptura() {
  clearInterval(vigilante);
  vigilante = setInterval(() => {
    if (!capturando()) {
      clearInterval(vigilante);
      actualizarMicrofonos();
    }
  }, 400);
}

function alternarVoz(boton, { alParcial, alFinal }) {
  if (capturando()) {
    detenerCaptura();
    actualizarMicrofonos();
    return;
  }
  microfonoActivo = boton;
  const empezo = iniciarCaptura({
    alParcial,
    alFinal: (texto) => { actualizarMicrofonos(); alFinal(texto); },
    alError: (mensaje) => { actualizarMicrofonos(); mostrarMensaje(mensaje, true); },
  });
  if (empezo) {
    actualizarMicrofonos();
    vigilarCaptura();
  }
}

/* ---------- Vista: nuevo reporte ---------- */

const tarjeta = crearTarjetaViva({
  tarjeta: porPrueba('tarjeta-viva'),
  textoPregunta: porRol('pregunta'),
  transcripcion: porRol('transcripcion'),
  alError: (mensaje) => mostrarMensaje(mensaje, true),
});

async function enviarFraseAlAsistente(texto) {
  const frase = texto.trim();
  if (!frase) return;
  const boton = porPrueba('boton-enviar-texto');
  boton.disabled = true;
  try {
    const respuesta = await tarjeta.enviarFrase(frase);
    if (respuesta.fuente === 'ninguna') {
      mostrarMensaje('No entendí del todo. Cuéntalo con otras palabras o completa la tarjeta a mano.');
    }
  } catch (error) {
    mostrarError(error);
  } finally {
    boton.disabled = false;
  }
}

function enviarTextoEscrito() {
  const campo = porPrueba('entrada-texto');
  const texto = campo.value;
  campo.value = '';
  enviarFraseAlAsistente(texto);
}

async function publicarReporte() {
  const boton = porPrueba('boton-publicar');
  boton.disabled = true;
  try {
    const resultado = await tarjeta.publicar();
    if (resultado.faltan) {
      mostrarMensaje(resultado.faltan);
      return;
    }
    ultimoPublicado = resultado.creado;
    await cargarCasos(true);
    navegar(`#/publicado/${encodeURIComponent(ultimoPublicado.id)}`);
  } catch (error) {
    mostrarError(error);
  } finally {
    boton.disabled = false;
  }
}

function montarSelector(nombre, estado, opciones = {}) {
  destruirSelector();
  if (!hayMapa()) return;
  selector = crearSelector(porPrueba(nombre), { estado, ...opciones });
  selector.invalidar();
}

function destruirSelector() {
  if (selector) {
    selector.destruir();
    selector = null;
  }
}

/* ---------- Vista: detalle ---------- */

let casoAbierto = null;

function pintarDetalle(caso) {
  casoAbierto = caso;
  const clase = CLASE_TIPO[caso.tipo] || 'perdida';
  const estado = porRol('caso-estado');
  estado.className = `estado ${clase}`;
  estado.textContent = ETIQUETA_TIPO[caso.tipo] || 'Caso';
  porRol('caso-foto').className = `foto foto-grande ${clase}`;
  porRol('caso-meta').textContent = `${caso.id} · ${haceCuanto(caso.fechaCreacion)}`;
  porRol('caso-nombre').textContent = tituloDe(caso);
  porRol('caso-aspecto').textContent = aspectoDe(caso);
  porRol('caso-zona').textContent = caso.zona;
  porRol('caso-referencia').textContent = vacio(caso.referencia) ? 'Sin dato' : caso.referencia;
  porRol('caso-cuenta').textContent = String(caso.cantidadAvistamientos ?? (caso.avistamientos || []).length);
  porRol('caso-descripcion').textContent = caso.descripcion || '';
  // Solo una pérdida admite avistamientos; de un hallazgo se habla directamente con quien lo encontró.
  porPrueba('boton-la-vi').hidden = caso.tipo === 'ENCONTRADA';
  porRol('caso-contacto').textContent = vacio(caso.contacto)
    ? ''
    : `Contacto: ${vacio(caso.nombreContacto) ? '' : `${caso.nombreContacto} · `}${caso.contacto}`;

  const pistas = caso.avistamientos || [];
  const contenedor = porRol('caso-pistas');
  if (pistas.length === 0) {
    contenedor.replaceChildren(el('p', 'vacio', 'Aún no hay pistas. Si la viste, cuéntanos.'));
  } else {
    contenedor.replaceChildren(...pistas.map((pista) => {
      const fila = el('div', 'pista');
      const texto = el('div');
      texto.append(
        el('b', '', vacio(pista.descripcion) ? pista.zona : pista.descripcion),
        el('span', '', `${pista.zona} · ${haceCuanto(pista.fechaHora)}`),
      );
      fila.append(el('i'), texto);
      return fila;
    }));
  }
}

async function abrirDetalle(id, ficha) {
  idSeleccionado = id;
  pintarLista();
  try {
    const caso = await consultarReporte(id);
    if (ficha !== rutaEnCurso) return;
    pintarDetalle(caso);
    if (mapa) mapa.mostrarCaso({ ...casos.find((c) => c.id === id), ...caso });
  } catch (error) {
    if (ficha !== rutaEnCurso) return;
    mostrarError(error);
    navegar('#/');
  }
}

/* ---------- Vista: avistamiento ---------- */

let coordenadasPista = { latitud: null, longitud: null };

async function abrirAvistamiento(id, ficha) {
  idSeleccionado = id;
  coordenadasPista = { latitud: null, longitud: null };
  try {
    const caso = casoAbierto && casoAbierto.id === id ? casoAbierto : await consultarReporte(id);
    if (ficha !== rutaEnCurso) return;
    casoAbierto = caso;
    const estado = porRol('pista-caso');
    estado.className = `estado ${CLASE_TIPO[caso.tipo] || 'perdida'}`;
    estado.textContent = `${tituloDe(caso)} · ${caso.id}`;
    porRol('pista-titulo').textContent = `¿Dónde viste a ${tituloDe(caso)}?`;
    porRol('pista-zona').value = caso.zona || '';
    porRol('pista-descripcion').value = '';
    porRol('pista-contacto').value = '';
    const vistaEn = typeof caso.latitud === 'number' && typeof caso.longitud === 'number'
      ? [caso.latitud, caso.longitud] : null;
    montarSelector('mapa-avistamiento', 'avistamiento', {
      vistaEn,
      alElegir: (latitud, longitud) => { coordenadasPista = { latitud, longitud }; },
    });
  } catch (error) {
    if (ficha !== rutaEnCurso) return;
    mostrarError(error);
    navegar('#/');
  }
}

async function enviarPista() {
  if (!casoAbierto) return;
  const boton = porPrueba('boton-enviar-pista');
  boton.disabled = true;
  const contacto = porRol('pista-contacto').value;
  try {
    await registrarAvistamiento(casoAbierto.id, {
      zona: porRol('pista-zona').value,
      referencia: null,
      latitud: coordenadasPista.latitud,
      longitud: coordenadasPista.longitud,
      descripcion: porRol('pista-descripcion').value,
      nombreContacto: null,
      medioContacto: vacio(contacto) ? null : contacto,
    });
    mostrarMensaje('Pista enviada. Gracias por avisar.');
    await cargarCasos(true);
    navegar(`#/caso/${encodeURIComponent(casoAbierto.id)}`);
  } catch (error) {
    mostrarError(error);
  } finally {
    boton.disabled = false;
  }
}

/* ---------- Vista: publicado ---------- */

function abrirPublicado(id) {
  idSeleccionado = id;
  const nombre = ultimoPublicado && ultimoPublicado.id === id ? ` · ${tituloDe(ultimoPublicado)}` : '';
  porRol('publicado-id').textContent = `${id}${nombre}`;
  pintarLista();
  if (mapa) {
    const caso = casos.find((c) => c.id === id);
    if (caso) mapa.mostrarCaso(caso);
  }
}

/* ---------- Enrutador ---------- */

const RUTAS = [
  [/^#?\/?$/, () => ({ vista: 'inicio' })],
  [/^#\/nuevo$/, () => ({ vista: 'nuevo' })],
  [/^#\/caso\/([^/]+)$/, (m) => ({ vista: 'caso', id: decodeURIComponent(m[1]) })],
  [/^#\/caso\/([^/]+)\/avistamiento$/, (m) => ({ vista: 'avistamiento', id: decodeURIComponent(m[1]) })],
  [/^#\/publicado\/([^/]+)$/, (m) => ({ vista: 'publicado', id: decodeURIComponent(m[1]) })],
];

function resolverRuta(hash) {
  for (const [patron, construir] of RUTAS) {
    const coincidencia = hash.match(patron);
    if (coincidencia) return construir(coincidencia);
  }
  return { vista: 'inicio' };
}

function enrutar() {
  const ficha = ++rutaEnCurso;
  const { vista, id } = resolverRuta(location.hash);
  detenerCaptura();
  clearInterval(vigilante);
  actualizarMicrofonos();
  if (vista !== 'nuevo' && vista !== 'avistamiento') destruirSelector();
  if (vista === 'inicio' || vista === 'nuevo') {
    idSeleccionado = null;
    if (mapa) mapa.limpiarPistas();
  }

  aplicacion.dataset.vista = vista;
  for (const nombre of ['inicio', 'nuevo', 'caso', 'avistamiento', 'publicado']) {
    panel(nombre).hidden = nombre !== vista;
  }
  aplicacion.dataset.hoja = '';
  porRol('pestana-casos').setAttribute('aria-pressed', 'false');
  panel(vista).scrollTop = 0;

  if (vista === 'inicio') {
    pintarLista();
    if (mapa) mapa.invalidar();
  } else if (vista === 'nuevo') {
    pintarLista();
    tarjeta.reiniciar();
    montarSelector('mapa-reporte', 'perdida', {
      alElegir: (latitud, longitud) => tarjeta.fijarCoordenadas(latitud, longitud),
    });
  } else if (vista === 'caso') {
    abrirDetalle(id, ficha);
  } else if (vista === 'avistamiento') {
    abrirAvistamiento(id, ficha);
  } else if (vista === 'publicado') {
    abrirPublicado(id);
  }
  document.title = vista === 'inicio' ? 'Pet Finder' : `${{
    nuevo: 'Nuevo reporte', caso: 'Caso', avistamiento: 'Reportar avistamiento', publicado: 'Reporte publicado',
  }[vista]} · Pet Finder`;
}

/* ---------- Eventos ---------- */

function conectarEventos() {
  porPrueba('boton-nuevo-reporte').addEventListener('click', () => navegar('#/nuevo'));
  porRol('pestana-reportar').addEventListener('click', () => navegar('#/nuevo'));
  porRol('cancelar').addEventListener('click', () => navegar('#/'));
  porRol('volver').addEventListener('click', () => navegar('#/'));
  porRol('cancelar-pista').addEventListener('click', () => {
    navegar(casoAbierto ? `#/caso/${encodeURIComponent(casoAbierto.id)}` : '#/');
  });
  porPrueba('boton-la-vi').addEventListener('click', () => {
    if (casoAbierto) navegar(`#/caso/${encodeURIComponent(casoAbierto.id)}/avistamiento`);
  });
  porPrueba('boton-enviar-pista').addEventListener('click', enviarPista);
  porPrueba('boton-publicar').addEventListener('click', publicarReporte);
  porPrueba('boton-enviar-texto').addEventListener('click', enviarTextoEscrito);
  porPrueba('entrada-texto').addEventListener('keydown', (evento) => {
    if (evento.key === 'Enter') {
      evento.preventDefault();
      enviarTextoEscrito();
    }
  });
  porRol('compartir').addEventListener('click', () => {
    if (casoAbierto) compartir(`${tituloDe(casoAbierto)} · Pet Finder`, `#/caso/${encodeURIComponent(casoAbierto.id)}`);
  });
  porRol('compartir-publicado').addEventListener('click', () => {
    if (ultimoPublicado) compartir(`${tituloDe(ultimoPublicado)} · Pet Finder`, `#/caso/${encodeURIComponent(ultimoPublicado.id)}`);
  });
  porRol('ver-caso').addEventListener('click', () => {
    const id = ultimoPublicado ? ultimoPublicado.id : idSeleccionado;
    if (id) navegar(`#/caso/${encodeURIComponent(id)}`);
  });

  porRol('buscar').addEventListener('input', (evento) => {
    busqueda = evento.target.value;
    pintarLista();
  });
  for (const boton of document.querySelectorAll('[data-filtro]')) {
    boton.addEventListener('click', () => {
      filtro = boton.dataset.filtro;
      for (const otro of document.querySelectorAll('[data-filtro]')) {
        const activo = otro === boton;
        otro.classList.toggle('on', activo);
        otro.setAttribute('aria-pressed', String(activo));
      }
      pintarLista();
    });
  }
  porRol('pestana-casos').addEventListener('click', (evento) => {
    const abierta = aplicacion.dataset.hoja === 'lista';
    aplicacion.dataset.hoja = abierta ? '' : 'lista';
    evento.currentTarget.setAttribute('aria-pressed', String(!abierta));
  });

  const microfonoNuevo = porPrueba('boton-microfono');
  const microfonoPista = porPrueba('boton-microfono-pista');
  for (const boton of [microfonoNuevo, microfonoPista]) {
    boton.dataset.etiqueta = boton.getAttribute('aria-label');
    boton.hidden = !soportaVoz();
  }
  microfonoNuevo.addEventListener('click', () => alternarVoz(microfonoNuevo, {
    alParcial: (texto) => tarjeta.mostrarTranscripcion(texto),
    alFinal: enviarFraseAlAsistente,
  }));
  microfonoPista.addEventListener('click', () => alternarVoz(microfonoPista, {
    alParcial: () => {},
    alFinal: (texto) => { porRol('pista-descripcion').value = texto; },
  }));

  window.addEventListener('hashchange', enrutar);
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) cargarCasos();
  });
  setInterval(() => { if (!document.hidden) cargarCasos(); }, CADA_CUANTO_ACTUALIZAR);
}

async function arrancar() {
  conectarEventos();
  iniciarMapa();
  await cargarCasos(true);
  enrutar();
}

arrancar();
