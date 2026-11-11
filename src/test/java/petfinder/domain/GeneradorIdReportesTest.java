package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.model.GeneradorIdReportes;

class GeneradorIdReportesTest {

    @Test
    @DisplayName("El primer identificador es PF-001")
    void primerIdentificador() {
        assertEquals("PF-001", new GeneradorIdReportes().siguiente());
    }

    @Test
    @DisplayName("Después de PF-999 sigue PF-1000 (valor límite del formato)")
    void pasaDeTresACuatroDigitos() {
        // Arrange
        GeneradorIdReportes generador = new GeneradorIdReportes();
        for (int i = 0; i < 999; i++) {
            generador.siguiente();
        }
        // Act
        String siguiente = generador.siguiente();
        // Assert
        assertEquals("PF-1000", siguiente);
    }

    @Test
    @DisplayName("Con la base en PF-041, el siguiente identificador es PF-042")
    void sigueDespuesDelUltimoGuardado() {
        // Arrange
        GeneradorIdReportes generador = new GeneradorIdReportes(41);
        // Act
        String siguiente = generador.siguiente();
        // Assert
        assertEquals("PF-042", siguiente);
    }

    @Test
    @DisplayName("numeroDe compara como número: PF-1000 es mayor que PF-999")
    void numeroDeComparaComoNumero() {
        // Act
        int mayor = Math.max(GeneradorIdReportes.numeroDe("PF-999"), GeneradorIdReportes.numeroDe("PF-1000"));
        // Assert
        assertEquals(1000, mayor);
    }

    @Test
    @DisplayName("numeroDe devuelve 0 si el texto no tiene el formato PF-número")
    void numeroDeSinFormato() {
        // Act y Assert
        assertEquals(0, GeneradorIdReportes.numeroDe("PF-T01"));
        assertEquals(0, GeneradorIdReportes.numeroDe(null));
    }

    @Test
    @DisplayName("Un último número negativo se rechaza")
    void rechazaNegativo() {
        // Act y Assert
        assertThrows(IllegalArgumentException.class, () -> new GeneradorIdReportes(-1));
    }

    @Test
    @DisplayName("1000 llamadas desde 8 hilos no repiten ningún identificador")
    void esSeguroEntreHilos() throws InterruptedException {
        // Arrange
        GeneradorIdReportes generador = new GeneradorIdReportes();
        Set<String> vistos = ConcurrentHashMap.newKeySet();
        ExecutorService hilos = Executors.newFixedThreadPool(8);
        // Act
        for (int i = 0; i < 1000; i++) {
            hilos.submit(() -> vistos.add(generador.siguiente()));
        }
        hilos.shutdown();
        hilos.awaitTermination(10, TimeUnit.SECONDS);
        // Assert
        assertEquals(1000, vistos.size());
    }
}
