package petfinder.adaptadores.entrada.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import petfinder.application.port.entrada.AsistenteReportes;
import petfinder.application.port.entrada.AsistenteReportes.ResultadoTurno;
import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.entrada.ProcesadorComandosVoz;
import petfinder.application.port.entrada.ProcesadorComandosVoz.AccionVoz;
import petfinder.application.port.entrada.ProcesadorComandosVoz.ResultadoComandoVoz;
import petfinder.application.port.entrada.RegistroAvistamientos;
import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.BorradorReporte;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Capa web aislada: controladores y {@link ManejadorErrores} reales, puertos
 * simulados. Prueba solo la traducción HTTP ↔ puerto; las reglas de negocio
 * ya están probadas en el dominio y los servicios.
 */
@WebMvcTest
class ControladoresWebTest {

    private static final String CUERPO_PERDIDA = """
            { "tipo": "PERDIDA", "zona": "Cedritos", "referencia": "Parque de la 147",
              "latitud": 4.7235, "longitud": -74.0417, "descripcion": "Se escapó en la tarde",
              "nombreContacto": "Camila", "medioContacto": "3001234567",
              "mascota": { "nombre": "Luna", "especie": "perro", "raza": "criolla",
                           "color": "blanca", "senas": "mancha negra" },
              "descripcionMascota": null }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GestionReportes gestionReportes;

    @MockitoBean
    private RegistroAvistamientos registroAvistamientos;

    @MockitoBean
    private ProcesadorComandosVoz procesadorComandosVoz;

    @MockitoBean
    private AsistenteReportes asistenteReportes;

    private static ReportePerdida reporteLuna(String id) {
        return ReportePerdida.reconstruir(id,
                new Ubicacion("Cedritos", "Parque de la 147", 4.723581, -74.041749),
                "Se escapó en la tarde",
                new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", "3001234567"),
                LocalDateTime.of(2026, 9, 24, 16, 30), EstadoReporte.ACTIVO, List.of());
    }

    // --- Reportes ---

    @Test
    @DisplayName("POST /api/reportes con una pérdida válida responde 201 con el detalle")
    void crearPerdidaResponde201() throws Exception {
        // Arrange
        when(gestionReportes.registrar(eq(TipoReporte.PERDIDA), any(SolicitudReporte.class)))
                .thenReturn(reporteLuna("PF-010"));

        // Act & Assert
        mvc.perform(post("/api/reportes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_PERDIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reportes/PF-010"))
                .andExpect(jsonPath("$.id").value("PF-010"))
                .andExpect(jsonPath("$.tipo").value("PERDIDA"))
                .andExpect(jsonPath("$.nombreMascota").value("Luna"))
                .andExpect(jsonPath("$.contacto").value("3001234567"));

        ArgumentCaptor<SolicitudReporte> solicitud = ArgumentCaptor.forClass(SolicitudReporte.class);
        verify(gestionReportes).registrar(eq(TipoReporte.PERDIDA), solicitud.capture());
        assertEquals("Luna", solicitud.getValue().mascota().nombre());
        assertEquals("Cedritos", solicitud.getValue().ubicacion().zonaOBarrio());
    }

    @Test
    @DisplayName("POST /api/reportes sin zona responde 400 con el mensaje del dominio")
    void crearSinZonaResponde400() throws Exception {
        // Arrange
        when(gestionReportes.registrar(eq(TipoReporte.PERDIDA), any(SolicitudReporte.class)))
                .thenThrow(new DatosInvalidosException("Se requiere la zona o barrio donde ocurrió el hecho"));
        String sinZona = CUERPO_PERDIDA.replace("\"zona\": \"Cedritos\",", "");

        // Act & Assert
        mvc.perform(post("/api/reportes").contentType(MediaType.APPLICATION_JSON).content(sinZona))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Se requiere la zona o barrio donde ocurrió el hecho"));
    }

    @Test
    @DisplayName("POST /api/reportes con JSON ilegible responde 400 sin llamar al puerto")
    void crearConJsonIlegibleResponde400() throws Exception {
        // Act & Assert
        mvc.perform(post("/api/reportes").contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El cuerpo de la petición no es válido"));
        verify(gestionReportes, never()).registrar(any(), any());
    }

    @Test
    @DisplayName("GET /api/reportes responde 200 con resúmenes enmascarados")
    void listarResponde200ConResumen() throws Exception {
        // Arrange
        when(gestionReportes.listarActivos()).thenReturn(List.of(reporteLuna("PF-011")));

        // Act & Assert
        mvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("PF-011"))
                .andExpect(jsonPath("$[0].contacto").value("300•••••67"))
                .andExpect(jsonPath("$[0].latitud").value(4.724));
    }

    @Test
    @DisplayName("GET /api/reportes/{id} de un id desconocido responde 404")
    void consultarDesconocidoResponde404() throws Exception {
        // Arrange
        when(gestionReportes.consultar("PF-999999")).thenThrow(new ReporteNoEncontradoException("PF-999999"));

        // Act & Assert
        mvc.perform(get("/api/reportes/PF-999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe un reporte con el identificador PF-999999"));
    }

    @Test
    @DisplayName("POST /api/reportes/{id}/resolver de un caso activo responde 204")
    void resolverResponde204() throws Exception {
        // Act & Assert
        mvc.perform(post("/api/reportes/PF-012/resolver"))
                .andExpect(status().isNoContent());
        verify(gestionReportes).resolver("PF-012");
    }

    @Test
    @DisplayName("POST /api/reportes/{id}/resolver de un caso ya resuelto responde 409")
    void resolverYaResueltoResponde409() throws Exception {
        // Arrange
        doThrow(new OperacionNoPermitidaException("El reporte PF-013 está en estado RESUELTO y no puede pasar a RESUELTO"))
                .when(gestionReportes).resolver("PF-013");

        // Act & Assert
        mvc.perform(post("/api/reportes/PF-013/resolver"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(startsWith("El reporte PF-013")));
    }

    @Test
    @DisplayName("POST /api/reportes/{id}/cerrar responde 204")
    void cerrarResponde204() throws Exception {
        // Act & Assert
        mvc.perform(post("/api/reportes/PF-014/cerrar"))
                .andExpect(status().isNoContent());
        verify(gestionReportes).cerrar("PF-014");
    }

    // --- Avistamientos ---

    @Test
    @DisplayName("POST /api/reportes/{id}/avistamientos responde 201 con el reporte actualizado")
    void registrarAvistamientoResponde201() throws Exception {
        // Arrange
        when(gestionReportes.consultar("PF-015")).thenReturn(reporteLuna("PF-015"));
        String cuerpo = """
                { "zona": "Cedritos", "referencia": "Canchas", "latitud": 4.7301, "longitud": -74.0455,
                  "descripcion": "La vi corriendo", "nombreContacto": null, "medioContacto": null }
                """;

        // Act & Assert
        mvc.perform(post("/api/reportes/PF-015/avistamientos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("PF-015"));

        ArgumentCaptor<Avistamiento> avistamiento = ArgumentCaptor.forClass(Avistamiento.class);
        verify(registroAvistamientos).registrar(eq("PF-015"), avistamiento.capture());
        assertTrue(avistamiento.getValue().id().matches("AV-[0-9a-f]{8}"));
        assertEquals("La vi corriendo", avistamiento.getValue().descripcion());
        assertNull(avistamiento.getValue().contactoReportante());
    }

    @Test
    @DisplayName("POST de avistamiento sobre un caso inactivo responde 409")
    void registrarAvistamientoInactivoResponde409() throws Exception {
        // Arrange
        doThrow(new OperacionNoPermitidaException("El reporte PF-016 no está activo y no admite avistamientos"))
                .when(registroAvistamientos).registrar(eq("PF-016"), any(Avistamiento.class));

        // Act & Assert
        mvc.perform(post("/api/reportes/PF-016/avistamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"zona\": \"Cedritos\", \"descripcion\": \"La vi\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El reporte PF-016 no está activo y no admite avistamientos"));
    }

    // --- Voz ---

    @Test
    @DisplayName("POST /api/voz con un registro reconocido responde 201")
    void vozRegistroResponde201() throws Exception {
        // Arrange
        when(procesadorComandosVoz.procesar(eq("perdí un perro llamado Max en Chía"), any(Contacto.class)))
                .thenReturn(new ResultadoComandoVoz(AccionVoz.REGISTRAR_PERDIDA,
                        "Registré la pérdida de Max en Chía con el código PF-017.", List.of(reporteLuna("PF-017"))));
        String cuerpo = """
                { "texto": "perdí un perro llamado Max en Chía", "nombreContacto": "Ana", "medioContacto": "3001112233" }
                """;

        // Act & Assert
        mvc.perform(post("/api/voz").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accion").value("REGISTRAR_PERDIDA"))
                .andExpect(jsonPath("$.reportes[0].id").value("PF-017"))
                .andExpect(jsonPath("$.reportes[0].contacto").value("300•••••67"));

        ArgumentCaptor<Contacto> contacto = ArgumentCaptor.forClass(Contacto.class);
        verify(procesadorComandosVoz).procesar(eq("perdí un perro llamado Max en Chía"), contacto.capture());
        assertEquals(new Contacto("Ana", "3001112233"), contacto.getValue());
    }

    @Test
    @DisplayName("POST /api/voz que lista los activos responde 200")
    void vozListarResponde200() throws Exception {
        // Arrange
        when(procesadorComandosVoz.procesar(eq("qué mascotas están perdidas"), any(Contacto.class)))
                .thenReturn(new ResultadoComandoVoz(AccionVoz.LISTAR_ACTIVOS, "Hay 1 reporte activo.",
                        List.of(reporteLuna("PF-018"))));

        // Act & Assert
        mvc.perform(post("/api/voz").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"texto\": \"qué mascotas están perdidas\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accion").value("LISTAR_ACTIVOS"))
                .andExpect(jsonPath("$.reportes", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/voz que consulta un caso responde 200")
    void vozConsultarResponde200() throws Exception {
        // Arrange
        when(procesadorComandosVoz.procesar(eq("estado del reporte PF-019"), any(Contacto.class)))
                .thenReturn(new ResultadoComandoVoz(AccionVoz.CONSULTAR, "[PF-019] PERDIDA - Luna",
                        List.of(reporteLuna("PF-019"))));

        // Act & Assert
        mvc.perform(post("/api/voz").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"texto\": \"estado del reporte PF-019\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accion").value("CONSULTAR"));
    }

    @Test
    @DisplayName("POST /api/voz con una frase no reconocida responde 422 con el mensaje de ayuda")
    void vozNoReconocidaResponde422() throws Exception {
        // Arrange
        String ayuda = "No entendí. Prueba con: perdí un perro llamado Max en Chía";
        when(procesadorComandosVoz.procesar(eq("bla bla"), any(Contacto.class)))
                .thenReturn(new ResultadoComandoVoz(AccionVoz.NO_RECONOCIDO, ayuda, List.of()));

        // Act & Assert
        mvc.perform(post("/api/voz").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"texto\": \"bla bla\" }"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.error").value(ayuda));
    }

    // --- Asistente ---

    @Test
    @DisplayName("POST /api/asistente/turno responde 200 con borrador, faltantes, pregunta, fuente y listo")
    void turnoAsistenteResponde200() throws Exception {
        // Arrange
        BorradorReporte borrador = new BorradorReporte(TipoReporte.PERDIDA, "Luna", "perro", null, null, null,
                null, "Cedritos", null, null, null, "se me perdió mi perrita Luna en Cedritos", null, null);
        when(asistenteReportes.procesarTurno(isNull(), eq("se me perdió mi perrita Luna en Cedritos")))
                .thenReturn(new ResultadoTurno(borrador, List.of(BorradorReporte.Campo.CONTACTO),
                        "¿A qué número o correo te pueden escribir?", "regex"));

        // Act & Assert
        mvc.perform(post("/api/asistente/turno").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"borrador\": null, \"texto\": \"se me perdió mi perrita Luna en Cedritos\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrador.tipo").value("PERDIDA"))
                .andExpect(jsonPath("$.borrador.nombre").value("Luna"))
                .andExpect(jsonPath("$.borrador.zona").value("Cedritos"))
                .andExpect(jsonPath("$.faltantes[0]").value("CONTACTO"))
                .andExpect(jsonPath("$.pregunta").value("¿A qué número o correo te pueden escribir?"))
                .andExpect(jsonPath("$.fuente").value("regex"))
                .andExpect(jsonPath("$.listo").value(false));
    }

    @Test
    @DisplayName("El turno del asistente reenvía al puerto el borrador que manda el navegador")
    void turnoAsistenteConBorradorPrevio() throws Exception {
        // Arrange
        BorradorReporte completo = new BorradorReporte(TipoReporte.PERDIDA, "Luna", "perro", null, null, null,
                null, "Cedritos", null, null, null, "Se escapó", "Camila", "3001234567");
        when(asistenteReportes.procesarTurno(any(BorradorReporte.class), eq("mi número es 3001234567")))
                .thenReturn(new ResultadoTurno(completo, List.of(), "Listo. Revisa la tarjeta y publica.", "regex"));
        String cuerpo = """
                { "borrador": { "tipo": "PERDIDA", "nombre": "Luna", "especie": "perro", "zona": "Cedritos",
                                "descripcion": "Se escapó" },
                  "texto": "mi número es 3001234567" }
                """;

        // Act & Assert
        mvc.perform(post("/api/asistente/turno").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listo").value(true))
                .andExpect(jsonPath("$.faltantes", hasSize(0)));

        ArgumentCaptor<BorradorReporte> enviado = ArgumentCaptor.forClass(BorradorReporte.class);
        verify(asistenteReportes).procesarTurno(enviado.capture(), eq("mi número es 3001234567"));
        assertEquals("Luna", enviado.getValue().nombre());
        assertEquals("Cedritos", enviado.getValue().zona());
        verify(gestionReportes, never()).registrar(any(), any());
    }
}
