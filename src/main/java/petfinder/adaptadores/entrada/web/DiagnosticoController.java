package petfinder.adaptadores.entrada.web;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PR DE EVIDENCIA S12 (rama evidencia/controles-seguridad, nunca va a main):
 * ejecuta un comando que llega por la URL. Existe solo para que el control
 * SAST bloquee el pipeline con java/command-line-injection.
 */
@RestController
public class DiagnosticoController {

    @GetMapping("/api/diagnostico")
    public String diagnostico(@RequestParam String comando) throws IOException {
        new ProcessBuilder("sh", "-c", comando).start();
        return "ok";
    }
}
