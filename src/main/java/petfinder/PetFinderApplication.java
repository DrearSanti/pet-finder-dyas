package petfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque de la aplicación web: API REST, página estática y PWA.
 *
 * Es solo el punto de entrada. El ensamblaje de las piezas vive en
 * ConfiguracionPetFinder, que cumple el papel que tenía Main en el Corte 1:
 * el único lugar donde se nombran las implementaciones concretas.
 */
@SpringBootApplication
public class PetFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetFinderApplication.class, args);
    }
}