// La tarjeta viva: el borrador del reporte que se llena por voz, por texto o a mano.
// Publicar nunca ocurre aquí solo: solo el botón "Publicar reporte" llama a publicar().

import { turnoAsistente, crearReporte } from './api.js';

const ICONO_LISTO = '<svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="10" class="relleno-listo"/>'
  + '<path d="M7.5 12.5l3 3 6-6.5" class="trazo-listo" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>';

const ICONO_FALTA = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">'
  + '<circle cx="12" cy="12" r="9" class="trazo-falta" stroke-width="2" stroke-dasharray="3 3"/></svg>';

const VACIO = Object.freeze({
  tipo: null, nombre: null, especie: null, raza: null, color: null, senas: null, descripcionMascota: null,
  zona: null, referencia: null, latitud: null, longitud: null, descripcion: null, contactoNombre: null, contactoMedio: null,
});

/**
 * Filas de la tarjeta. `para` limita la fila a un tipo de reporte; `requerido` es lo que el servidor exige
 * (BorradorReporte.camposFaltantes): aquí solo sirve para mostrar "Falta", el servidor sigue siendo quien decide.
 */
const FILAS = [
  { campo: 'tipo', rotulo: 'Reporte', punto: 'pt-perdida', requerido: true },
  { campo: 'nombre', rotulo: 'Nombre', punto: 'pt-acento', para: 'PERDIDA', requerido: true },
  { campo: 'especie', rotulo: 'Especie', punto: 'pt-encontrada', para: 'PERDIDA', requerido: true },
  { campo: 'descripcionMascota', rotulo: 'Animal', punto: 'pt-encontrada', para: 'ENCONTRADA', requerido: true },
  { campo: 'zona', rotulo: 'Zona', punto: 'pt-resuelto', requerido: true },
  { campo: 'descripcion', rotulo: 'Qué pasó', punto: 'pt-avistamiento', requerido: true },
  { campo: 'contactoMedio', rotulo: 'Contacto', punto: 'pt-perdida', requerido: true },
  { campo: 'contactoNombre', rotulo: 'Tu nombre', punto: 'pt-neutro', requerido: false },
];

/** Qué fila mostrar para cada dato que el servidor puede pedir. */
const FILA_DE_CAMPO = {
  TIPO: 'tipo', NOMBRE: 'nombre', ESPECIE: 'especie', DESCRIPCION_MASCOTA: 'descripcionMascota',
  ZONA: 'zona', DESCRIPCION: 'descripcion', CONTACTO: 'contactoMedio',
};

/** Color con el que se resalta en la transcripción la palabra que llenó cada campo. */
const RESALTE = { nombre: 'c-acento', especie: 'c-encontrada', zona: 'c-resuelto', contactoMedio: 'c-perdida', descripcionMascota: 'c-avistamiento' };

function el(etiqueta, clase, texto) {
  const nodo = document.createElement(etiqueta);
  if (clase) nodo.className = clase;
  if (texto !== undefined) nodo.textContent = texto;
  return nodo;
}

function vacio(valor) {
  return valor === null || valor === undefined || String(valor).trim() === '';
}

/**
 * @param tarjeta       contenedor de las filas (data-prueba="tarjeta-viva")
 * @param textoPregunta elemento donde se escribe la pregunta del asistente
 * @param transcripcion elemento donde se muestra lo que la persona dijo o escribió
 * @param alError       recibe el mensaje de un ErrorApi; la tarjeta nunca usa alert()
 */
export function crearTarjetaViva({ tarjeta, textoPregunta, transcripcion, alError = () => {} }) {
  let borrador = { ...VACIO };
  let pregunta = '';
  let faltantes = [];
  let firma = '';
  let temporizador = null;
  const filas = new Map();

  function tipoActual() {
    return borrador.tipo;
  }

  function filasVisibles() {
    const aspecto = [borrador.color, borrador.raza, borrador.senas].filter((v) => !vacio(v)).join(' · ');
    const visibles = FILAS.filter((f) => !f.para || f.para === (tipoActual() || 'PERDIDA'));
    return { visibles, aspecto };
  }

  function construirFila(definicion) {
    const raiz = el(definicion.campo === 'tipo' ? 'div' : 'label', 'campo');
    raiz.dataset.prueba = `campo-${definicion.campo}`;
    raiz.append(el('i', `pt ${definicion.punto}`), el('span', 'k', definicion.rotulo));
    let control;
    if (definicion.campo === 'tipo') {
      control = el('div', 'segmentado v');
      for (const [valor, texto] of [['PERDIDA', 'Perdida'], ['ENCONTRADA', 'Encontrada']]) {
        const boton = el('button', '', texto);
        boton.type = 'button';
        boton.dataset.valor = valor;
        boton.addEventListener('click', () => editar('tipo', valor));
        control.append(boton);
      }
    } else {
      control = el('input', 'v');
      control.type = 'text';
      control.autocomplete = 'off';
      control.setAttribute('aria-label', definicion.rotulo);
      control.placeholder = definicion.requerido ? 'Falta' : 'Opcional';
      control.addEventListener('input', () => editar(definicion.campo, control.value, false));
    }
    const marca = el('span', 'estado-campo');
    raiz.append(control, marca);
    filas.set(definicion.campo, { raiz, control, marca, definicion });
    return raiz;
  }

  function construirAspecto() {
    const raiz = el('div', 'campo');
    raiz.dataset.prueba = 'campo-aspecto';
    const valor = el('span', 'v');
    raiz.append(el('i', 'pt pt-avistamiento'), el('span', 'k', 'Aspecto'), valor);
    filas.set('aspecto', { raiz, control: valor, marca: null, definicion: { campo: 'aspecto' } });
    return raiz;
  }

  /** Reconstruye solo si cambian las filas visibles; así escribir en un campo no le quita el foco. */
  function asegurarFilas() {
    const { visibles, aspecto } = filasVisibles();
    const nuevaFirma = visibles.map((f) => f.campo).join('|') + (aspecto ? '|aspecto' : '');
    if (nuevaFirma === firma) return;
    firma = nuevaFirma;
    filas.clear();
    tarjeta.replaceChildren(...visibles.map(construirFila), ...(aspecto ? [construirAspecto()] : []));
  }

  function pintar(previo = null) {
    asegurarFilas();
    const { aspecto } = filasVisibles();
    for (const [campo, fila] of filas) {
      const valor = campo === 'aspecto' ? aspecto : borrador[campo];
      const cambio = previo !== null && String(valor ?? '') !== String((campo === 'aspecto' ? '' : previo[campo]) ?? '');
      if (campo === 'tipo') {
        for (const boton of fila.control.children) {
          boton.setAttribute('aria-pressed', String(boton.dataset.valor === valor));
          boton.classList.toggle('on', boton.dataset.valor === valor);
        }
      } else if (campo === 'aspecto') {
        fila.control.textContent = valor;
      } else if (document.activeElement !== fila.control) {
        fila.control.value = valor ?? '';
      }
      if (fila.marca) marcar(fila, valor);
      if (cambio && campo !== 'aspecto') iluminar(fila.raiz);
    }
    textoPregunta.textContent = pregunta;
  }

  /** ✓ verde si está completo, círculo punteado naranja si el servidor lo exige y aún no está (DESIGN.md §8). */
  function marcar(fila, valor) {
    fila.marca.replaceChildren();
    fila.raiz.classList.toggle('falta', fila.definicion.requerido && vacio(valor));
    if (!vacio(valor)) {
      fila.marca.insertAdjacentHTML('beforeend', ICONO_LISTO);
    } else if (fila.definicion.requerido) {
      fila.marca.insertAdjacentHTML('beforeend', ICONO_FALTA);
    }
  }

  /** La animación de §9 de DESIGN.md: el campo que acaba de llenarse se ilumina y se apaga. */
  function iluminar(raiz) {
    raiz.classList.remove('nuevo');
    void raiz.offsetWidth;
    raiz.classList.add('nuevo');
  }

  function aplicar(respuesta, previo) {
    borrador = { ...VACIO, ...respuesta.borrador };
    faltantes = respuesta.faltantes || [];
    pregunta = respuesta.pregunta || '';
    pintar(previo);
  }

  /** Pinta con color, dentro de lo que la persona dijo, las palabras que llenaron un campo. */
  function mostrarTranscripcion(texto, resaltar = false) {
    if (!resaltar) {
      transcripcion.textContent = texto;
      return;
    }
    const pares = Object.entries(RESALTE)
      .map(([campo, clase]) => ({ valor: borrador[campo], clase }))
      .filter((p) => !vacio(p.valor));
    const minusculas = texto.toLowerCase();
    const tramos = [];
    for (const par of pares) {
      const inicio = minusculas.indexOf(String(par.valor).toLowerCase());
      if (inicio >= 0) tramos.push({ inicio, fin: inicio + String(par.valor).length, clase: par.clase });
    }
    tramos.sort((a, b) => a.inicio - b.inicio);
    const nodos = [];
    let cursor = 0;
    for (const tramo of tramos) {
      if (tramo.inicio < cursor) continue;
      if (tramo.inicio > cursor) nodos.push(document.createTextNode(texto.slice(cursor, tramo.inicio)));
      nodos.push(el('b', tramo.clase, texto.slice(tramo.inicio, tramo.fin)));
      cursor = tramo.fin;
    }
    if (cursor < texto.length) nodos.push(document.createTextNode(texto.slice(cursor)));
    transcripcion.replaceChildren(...nodos);
  }

  /** Una frase (dicha o escrita) va al asistente y devuelve lo que entendió. Lanza ErrorApi si el servidor falla. */
  async function enviarFrase(texto) {
    const previo = borrador;
    const respuesta = await turnoAsistente(borrador, texto);
    aplicar(respuesta, previo);
    mostrarTranscripcion(texto, true);
    return respuesta;
  }

  /**
   * Cambia un campo a mano. Con `repintar` falso no se toca la caja donde la persona está escribiendo.
   * Después se le pide al servidor recalcular qué falta: la regla vive en un solo lugar.
   */
  function editar(campo, valor, repintar = true) {
    const previo = borrador;
    borrador = { ...borrador, [campo]: vacio(valor) ? null : valor };
    if (repintar) pintar(previo);
    else if (filas.has(campo) && filas.get(campo).marca) marcar(filas.get(campo), borrador[campo]);
    clearTimeout(temporizador);
    temporizador = setTimeout(refrescar, 500);
  }

  async function refrescar() {
    try {
      const respuesta = await turnoAsistente(borrador, '');
      faltantes = respuesta.faltantes || [];
      pregunta = respuesta.pregunta || '';
      textoPregunta.textContent = pregunta;
    } catch (error) {
      alError(error.message);
    }
  }

  function enfocarPrimeraFalta() {
    const fila = filas.get(FILA_DE_CAMPO[faltantes[0]]);
    if (!fila) return;
    const destino = fila.control.matches('input') ? fila.control : fila.control.querySelector('button');
    if (destino) destino.focus();
  }

  /**
   * Publica el borrador. Si falta algo no llama a crearReporte: devuelve la pregunta para explicarlo,
   * porque un botón apagado no le dice a la persona qué hacer (DESIGN.md §8).
   */
  async function publicar() {
    clearTimeout(temporizador);
    await refrescar();
    if (faltantes.length > 0) {
      enfocarPrimeraFalta();
      return { faltan: pregunta };
    }
    const b = borrador;
    const creado = await crearReporte({
      tipo: b.tipo,
      zona: b.zona,
      referencia: b.referencia,
      latitud: b.latitud,
      longitud: b.longitud,
      descripcion: b.descripcion,
      nombreContacto: b.contactoNombre,
      medioContacto: b.contactoMedio,
      mascota: b.tipo === 'PERDIDA'
        ? { nombre: b.nombre, especie: b.especie, raza: b.raza, color: b.color, senas: b.senas }
        : null,
      descripcionMascota: b.tipo === 'ENCONTRADA' ? b.descripcionMascota : null,
    });
    return { creado };
  }

  function fijarCoordenadas(latitud, longitud) {
    borrador = { ...borrador, latitud, longitud };
  }

  function restablecer() {
    clearTimeout(temporizador);
    borrador = { ...VACIO };
    faltantes = [];
    pregunta = '';
    firma = '';
    transcripcion.textContent = '';
    pintar();
  }

  /** Tarjeta en blanco. La primera pregunta la pone el servidor, para no repetir su texto aquí. */
  function reiniciar() {
    restablecer();
    return refrescar();
  }

  restablecer();
  return { enviarFrase, publicar, fijarCoordenadas, reiniciar, mostrarTranscripcion, borrador: () => borrador };
}
