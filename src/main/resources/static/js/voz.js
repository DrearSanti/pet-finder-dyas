// Captura de voz con Web Speech. El reconocimiento ocurre en el navegador:
// al servidor solo llega el texto, nunca el audio.

const Reconocedor = window.SpeechRecognition || window.webkitSpeechRecognition;
export function soportaVoz() { return Boolean(Reconocedor); }
// iniciarCaptura({ alParcial(texto), alFinal(texto), alError(mensaje) }) — lang 'es-CO', interimResults true.
// Solo funciona en contexto seguro: https o http://localhost. Firefox no lo soporta: la página deja el campo de texto.

// Mensajes para la persona: qué pasó y qué hacer, siempre con el texto como salida.
const MENSAJES_ERROR = {
  'not-allowed': 'No tenemos permiso para usar el micrófono. Actívalo en tu navegador o escribe lo que pasó.',
  'service-not-allowed': 'Tu navegador no permite dictar aquí. Escribe lo que pasó.',
  'no-speech': 'No te escuchamos. Intenta de nuevo o escribe lo que pasó.',
  'audio-capture': 'No encontramos un micrófono. Revisa que esté conectado o escribe lo que pasó.',
  'network': 'El dictado necesita conexión. Revisa tu internet o escribe lo que pasó.',
};
const MENSAJE_GENERICO = 'No pudimos escucharte. Intenta de nuevo o escribe lo que pasó.';

// Una sola captura a la vez: un segundo toque al micrófono no abre otra.
let activo = null;

export function iniciarCaptura({ alParcial = () => {}, alFinal = () => {}, alError = () => {} } = {}) {
  if (!soportaVoz()) {
    alError('Tu navegador no permite dictar. Escribe lo que pasó.');
    return false;
  }
  detenerCaptura();

  const reconocimiento = new Reconocedor();
  reconocimiento.lang = 'es-CO';
  reconocimiento.interimResults = true;
  reconocimiento.continuous = false;
  reconocimiento.maxAlternatives = 1;

  let textoFinal = '';

  reconocimiento.onresult = (evento) => {
    let parcial = '';
    for (let i = evento.resultIndex; i < evento.results.length; i++) {
      const resultado = evento.results[i];
      if (resultado.isFinal) textoFinal += resultado[0].transcript;
      else parcial += resultado[0].transcript;
    }
    // El parcial incluye lo ya confirmado para que la transcripción no "salte" en pantalla.
    alParcial((textoFinal + parcial).trim());
  };

  reconocimiento.onerror = (evento) => {
    // 'aborted' lo provoca detenerCaptura(): la persona lo pidió, no es un error.
    if (evento.error === 'aborted') return;
    alError(MENSAJES_ERROR[evento.error] || MENSAJE_GENERICO);
  };

  reconocimiento.onend = () => {
    if (activo === reconocimiento) activo = null;
    const texto = textoFinal.trim();
    if (texto) alFinal(texto);
  };

  activo = reconocimiento;
  try {
    reconocimiento.start();
  } catch {
    activo = null;
    alError(MENSAJE_GENERICO);
    return false;
  }
  return true;
}

// stop() entrega lo que ya se escuchó (onend llama a alFinal); no descarta la frase.
export function detenerCaptura() {
  if (!activo) return;
  const reconocimiento = activo;
  activo = null;
  reconocimiento.stop();
}

export function capturando() { return activo !== null; }
