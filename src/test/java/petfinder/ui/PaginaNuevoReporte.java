package petfinder.ui;

import static petfinder.ui.PaginaInicio.porPrueba;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object de la vista "Nuevo reporte": el campo de texto del asistente,
 * la tarjeta viva y el botón de publicar.
 *
 * Usa el campo de texto y no el micrófono a propósito: es el respaldo de la
 * voz, hace lo mismo y es lo único que un Chrome sin ventana puede operar. Las
 * filas de la tarjeta viva se generan en JavaScript con
 * {@code data-prueba="campo-<nombre>"}, así que se esperan antes de usarlas.
 */
public class PaginaNuevoReporte {

    private final WebDriver navegador;
    private final WebDriverWait espera;

    PaginaNuevoReporte(WebDriver navegador, WebDriverWait espera) {
        this.navegador = navegador;
        this.espera = espera;
        espera.until(ExpectedConditions.visibilityOfElementLocated(porPrueba("entrada-texto")));
    }

    private static By controlDelCampo(String campo) {
        return By.cssSelector("[data-prueba='campo-" + campo + "'] input");
    }

    /** Escribe una frase y la envía al asistente, como quien no quiere o no puede dictar. */
    public PaginaNuevoReporte escribir(String frase) {
        WebElement entrada = espera.until(ExpectedConditions.elementToBeClickable(porPrueba("entrada-texto")));
        entrada.clear();
        entrada.sendKeys(frase);
        navegador.findElement(porPrueba("boton-enviar-texto")).click();
        return this;
    }

    /** Espera a que el asistente llene un campo de la tarjeta viva con el valor esperado. */
    public PaginaNuevoReporte esperarCampo(String campo, String valor) {
        espera.until(ExpectedConditions.attributeToBe(controlDelCampo(campo), "value", valor));
        return this;
    }

    /** Completa a mano el contacto en la tarjeta viva, como hace la persona cuando el asistente lo pide. */
    public PaginaNuevoReporte completarContacto(String medio) {
        WebElement campo = espera.until(ExpectedConditions.elementToBeClickable(controlDelCampo("contactoMedio")));
        campo.clear();
        campo.sendKeys(medio);
        return this;
    }

    /** Toca "Publicar reporte". La página vuelve a pedir la lista antes de mostrar la confirmación. */
    public PaginaInicio publicar() {
        espera.until(ExpectedConditions.elementToBeClickable(porPrueba("boton-publicar"))).click();
        return new PaginaInicio(navegador);
    }
}
