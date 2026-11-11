// La PWA: el service worker deja abrir la interfaz sin conexión. Solo funciona en https o en localhost.
// Va en su propio archivo porque la política de seguridad (CSP) no permite scripts escritos dentro del HTML.
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {});
  });
}
