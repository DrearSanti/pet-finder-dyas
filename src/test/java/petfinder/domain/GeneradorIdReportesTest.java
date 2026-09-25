package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
