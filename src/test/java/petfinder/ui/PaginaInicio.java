package petfinder.ui;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object de la pantalla principal de Pet Finder: la lista de casos y el
 * panel de la derecha (detalle de un caso y formulario "La vi").
 *
 * Las pruebas nunca buscan elementos por su cuenta: piden acciones a esta
 * clase ("abre el caso", "envía la pista"). Si el HTML cambia, se corrige aquí
 * y no en cada prueba. Todos los selectores son los ganchos
 * {@code data-prueba} del contrato con la interfaz, que no dependen de clases
 * CSS ni de textos visibles, y toda espera es explícita con
 * {@link WebDriverWait}: la página trabaja con llamadas asíncronas al
 * servidor y una pausa fija sería o lenta o frágil.
 */
public class PaginaInicio {

    static final Duration ESPERA = Duration.ofSeconds(10);

    private final WebDriver navegador;
    private final WebDriverWait espera;

    public PaginaInicio(WebDriver navegador) {
        this.navegador = navegador;
        this.espera = new WebDriverWait(navegador, ESPERA);
        // La lista se repinta tras cada publicación: una tarjeta leída puede dejar de existir.
        this.espera.ignoring(StaleElementReferenceException.class);
    }

    static By porPrueba(String nombre) {
        return By.cssSelector("[data-prueba='" + nombre + "']");
    }

    private static By tarjetaDeCaso(String id) {
        return By.cssSelector("[data-prueba='lista-casos'] [data-prueba='tarjeta-caso'][data-id='" + id + "']");
    }

    /** Abre la app y espera a que la lista de casos esté en pantalla. */
    public PaginaInicio abrir(String urlBase) {
        navegador.get(urlBase + "/");
        espera.until(ExpectedConditions.visibilityOfElementLocated(porPrueba("lista-casos")));
        return this;
    }

    public PaginaNuevoReporte irANuevoReporte() {
        espera.until(ExpectedConditions.elementToBeClickable(porPrueba("boton-nuevo-reporte"))).click();
        return new PaginaNuevoReporte(navegador, espera);
    }

    /**
     * Espera a que la lista muestre un caso cuyo texto contenga el valor dado y
     * devuelve su id. La lista se repinta tras cada publicación, así que la
     * condición se vuelve a evaluar hasta que aparece. El id se lee dentro de
     * la misma espera: si se leyera después, un repintado entre la búsqueda y
     * la lectura dejaría la tarjeta obsoleta sin nadie que reintente.
     */
    public String esperarCasoQueDiga(String texto) {
        return espera.until(navegador -> navegador
                .findElements(By.cssSelector("[data-prueba='lista-casos'] [data-prueba='tarjeta-caso']"))
                .stream()
                .filter(elemento -> elemento.getText().contains(texto))
                .map(elemento -> elemento.getDomAttribute("data-id"))
                .findFirst()
                .orElse(null));
    }

    /** Abre el detalle de un caso tocando su tarjeta en la lista. */
    public PaginaInicio abrirCaso(String id) {
        espera.until(ExpectedConditions.elementToBeClickable(tarjetaDeCaso(id))).click();
        espera.until(ExpectedConditions.visibilityOfElementLocated(porPrueba("boton-la-vi")));
        return this;
    }

    /** "La vi": abre el formulario de avistamiento del caso abierto. */
    public PaginaInicio pulsarLaVi() {
        espera.until(ExpectedConditions.elementToBeClickable(porPrueba("boton-la-vi"))).click();
        espera.until(ExpectedConditions.visibilityOfElementLocated(porPrueba("boton-enviar-pista")));
        return this;
    }

    /** Envía la pista tal como la dejó la página (la zona viene llena con la del caso). */
    public PaginaInicio enviarPista() {
        espera.until(ExpectedConditions.elementToBeClickable(porPrueba("boton-enviar-pista"))).click();
        return this;
    }

    /**
     * Espera un aviso en la zona de mensajes y devuelve su texto. Los avisos se
     * borran solos a los pocos segundos, por eso se lee apenas aparece.
     */
    public String esperarMensajeQueDiga(String texto) {
        espera.until(ExpectedConditions.textToBePresentInElementLocated(porPrueba("mensaje"), texto));
        return navegador.findElement(porPrueba("mensaje")).getText();
    }
}
