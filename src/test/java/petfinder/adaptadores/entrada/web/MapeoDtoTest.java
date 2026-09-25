package petfinder.adaptadores.entrada.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import petfinder.adaptadores.entrada.web.ManejadorErrores.CuerpoError;
import petfinder.adaptadores.entrada.web.dto.AvistamientoDTO;
import petfinder.adaptadores.entrada.web.dto.ReporteDTO;
import petfinder.adaptadores.entrada.web.dto.ReporteDTO.AvistamientoPublicoDTO;
import petfinder.adaptadores.entrada.web.dto.SolicitudReporteDTO;
import petfinder.adaptadores.entrada.web.dto.SolicitudReporteDTO.MascotaDTO;
import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

class MapeoDtoTest {

    private static ReportePerdida reportePerdidaConAvistamiento(String medioContacto) {
        Avistamiento avistamiento = new Avistamiento("AV-1a2b3c4d", LocalDateTime.of(2026, 9, 24, 17, 10),
                new Ubicacion("Cedritos", "Canchas", 4.730123, -74.045567), "La vi corriendo",
                new Contacto("Pedro", "3109998877"));
        return ReportePerdida.reconstruir("PF-001",
                new Ubicacion("Cedritos", "Parque de la 147", 4.723581, -74.041749),
                "Se escapó en la tarde",
                new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", medioContacto),
                LocalDateTime.of(2026, 9, 24, 16, 30), EstadoReporte.ACTIVO, List.of(avistamiento));
    }

    // --- SolicitudReporteDTO ---

    @Test
    @DisplayName("Una solicitud PERDIDA se convierte en paraPerdida con su mascota")
    void solicitudPerdidaLlevaMascota() {
        // Arrange
        SolicitudReporteDTO dto = new SolicitudReporteDTO(TipoReporte.PERDIDA, "Cedritos", "Parque de la 147",
                4.7235, -74.0417, "Se escapó en la tarde", "Camila", "3001234567",
                new MascotaDTO("Luna", "perro", "criolla", "blanca", "mancha negra"), null);

        // Act
        SolicitudReporte solicitud = dto.aSolicitud();

        // Assert
        assertEquals(new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"), solicitud.mascota());
        assertNull(solicitud.descripcionMascota());
        assertEquals(new Ubicacion("Cedritos", "Parque de la 147", 4.7235, -74.0417), solicitud.ubicacion());
        assertEquals(new Contacto("Camila", "3001234567"), solicitud.contacto());
        assertEquals("Se escapó en la tarde", solicitud.descripcion());
    }

    @Test
    @DisplayName("Una solicitud ENCONTRADA se convierte en paraEncontrada con la descripción de la mascota")
    void solicitudEncontradaLlevaDescripcion() {
        // Arrange
        SolicitudReporteDTO dto = new SolicitudReporteDTO(TipoReporte.ENCONTRADA, "Chía", null, null, null,
                "Estaba en la puerta del supermercado", "Sofía", "sofia@correo.co",
                null, "Gata gris con collar rojo");

        // Act
        SolicitudReporte solicitud = dto.aSolicitud();

        // Assert
        assertNull(solicitud.mascota());
        assertEquals("Gata gris con collar rojo", solicitud.descripcionMascota());
        assertFalse(solicitud.ubicacion().tieneCoordenadas());
    }

    @Test
    @DisplayName("Una solicitud sin tipo se rechaza con un mensaje para la persona")
    void solicitudSinTipoSeRechaza() {
        // Arrange
        SolicitudReporteDTO dto = new SolicitudReporteDTO(null, "Cedritos", null, null, null,
                "Se perdió", "Camila", "3001234567", null, null);

        // Act
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class, dto::aSolicitud);

        // Assert
        assertEquals("Indica si la mascota se perdió o si la encontraste", error.getMessage());
    }

    // --- ReporteDTO.resumen ---

    @Test
    @DisplayName("El resumen redondea coordenadas a tres decimales y enmascara el teléfono")
    void resumenRedondeaYEnmascaraTelefono() {
        // Arrange
        ReportePerdida reporte = reportePerdidaConAvistamiento("3001234567");

        // Act
        ReporteDTO dto = ReporteDTO.resumen(reporte);

        // Assert
        assertEquals(4.724, dto.latitud());
        assertEquals(-74.042, dto.longitud());
        assertEquals("300•••••67", dto.contacto());
        assertEquals("Camila", dto.nombreContacto());
        assertEquals(TipoReporte.PERDIDA, dto.tipo());
        assertEquals("Luna", dto.nombreMascota());
        assertEquals("mancha negra", dto.senas());
        assertEquals(1, dto.cantidadAvistamientos());
        assertTrue(dto.avistamientos().isEmpty());
    }

    @Test
    @DisplayName("El resumen enmascara un correo dejando dos letras y el dominio")
    void resumenEnmascaraCorreo() {
        // Arrange
        ReportePerdida reporte = reportePerdidaConAvistamiento("sofia@correo.co");

        // Act
        ReporteDTO dto = ReporteDTO.resumen(reporte);

        // Assert
        assertEquals("so•••@correo.co", dto.contacto());
    }

    @ParameterizedTest(name = "\"{0}\" se muestra como \"{1}\"")
    @DisplayName("El enmascarado cubre teléfonos, correos y otros textos")
    @CsvSource({
            "3001234567, 300•••••67",
            "300 123 4567, 300•••••67",
            "601234, 601•34",
            "sofia@correo.co, so•••@correo.co",
            "a@b.co, a•••@b.co",
            "@camila_pets, @c•••",
            "Camila, Ca•••",
            "Luna, •••",
            "12345, •••"
    })
    void enmascarado(String medio, String esperado) {
        // Act
        String resultado = ReporteDTO.enmascarar(medio);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("Un reporte de encontrada se mapea sin mascota y con su descripción")
    void resumenDeEncontrada() {
        // Arrange
        ReporteEncontrada reporte = ReporteEncontrada.reconstruir("PF-002", new Ubicacion("Chía", null),
                "Estaba en la puerta del supermercado", "Gata gris con collar rojo",
                new Contacto("Sofía", "sofia@correo.co"), LocalDateTime.of(2026, 9, 24, 9, 0),
                EstadoReporte.ACTIVO);

        // Act
        ReporteDTO dto = ReporteDTO.resumen(reporte);

        // Assert
        assertEquals(TipoReporte.ENCONTRADA, dto.tipo());
        assertNull(dto.nombreMascota());
        assertNull(dto.latitud());
        assertEquals("Gata gris con collar rojo", dto.descripcionMascota());
        assertEquals("so•••@correo.co", dto.contacto());
        assertEquals(0, dto.cantidadAvistamientos());
    }

    // --- ReporteDTO.detalle ---

    @Test
    @DisplayName("El detalle conserva el contacto completo y lista los avistamientos")
    void detalleConservaContacto() {
        // Arrange
        ReportePerdida reporte = reportePerdidaConAvistamiento("3001234567");

        // Act
        ReporteDTO dto = ReporteDTO.detalle(reporte);

        // Assert
        assertEquals("3001234567", dto.contacto());
        assertEquals(4.723581, dto.latitud());
        assertEquals(1, dto.avistamientos().size());
        AvistamientoPublicoDTO avistamiento = dto.avistamientos().get(0);
        assertEquals("AV-1a2b3c4d", avistamiento.id());
        assertEquals("Canchas", avistamiento.referencia());
        assertEquals("La vi corriendo", avistamiento.descripcion());
    }

    @Test
    @DisplayName("Los avistamientos del detalle no exponen el contacto de quien los reportó")
    void detalleSinContactoDelAvistamiento() {
        // Arrange
        Set<String> campos = Arrays.stream(AvistamientoPublicoDTO.class.getRecordComponents())
                .map(componente -> componente.getName())
                .collect(Collectors.toSet());

        // Act
        ReporteDTO dto = ReporteDTO.detalle(reportePerdidaConAvistamiento("3001234567"));

        // Assert
        assertFalse(campos.contains("nombreContacto"));
        assertFalse(campos.contains("medioContacto"));
        assertFalse(campos.contains("contacto"));
        assertFalse(dto.avistamientos().toString().contains("3109998877"));
        assertFalse(dto.avistamientos().toString().contains("Pedro"));
    }

    // --- AvistamientoDTO ---

    @Test
    @DisplayName("Un avistamiento sin contacto se crea sin contacto, con el id y la hora del servidor")
    void avistamientoSinContacto() {
        // Arrange
        AvistamientoDTO dto = new AvistamientoDTO("Cedritos", "Canchas", 4.7301, -74.0455,
                "La vi corriendo", null, " ");
        LocalDateTime ahora = LocalDateTime.of(2026, 9, 24, 17, 10);

        // Act
        Avistamiento avistamiento = dto.aAvistamiento("AV-12345678", ahora);

        // Assert
        assertEquals("AV-12345678", avistamiento.id());
        assertEquals(ahora, avistamiento.fechaHora());
        assertEquals(new Ubicacion("Cedritos", "Canchas", 4.7301, -74.0455), avistamiento.ubicacion());
        assertNull(avistamiento.contactoReportante());
    }

    @Test
    @DisplayName("Un avistamiento con contacto lo conserva para el dominio")
    void avistamientoConContacto() {
        // Arrange
        AvistamientoDTO dto = new AvistamientoDTO("Cedritos", null, null, null,
                "La vi corriendo", "Pedro", "3109998877");

        // Act
        Avistamiento avistamiento = dto.aAvistamiento("AV-12345678", LocalDateTime.now());

        // Assert
        assertEquals(new Contacto("Pedro", "3109998877"), avistamiento.contactoReportante());
    }

    // --- ManejadorErrores ---

    @Test
    @DisplayName("Datos inválidos responde 400 con el mensaje del dominio")
    void datosInvalidosEs400() {
        // Arrange
        ManejadorErrores manejador = new ManejadorErrores();

        // Act
        ResponseEntity<CuerpoError> respuesta = manejador.datosInvalidos(
                new DatosInvalidosException("Se requiere la zona o barrio donde ocurrió el hecho"));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Se requiere la zona o barrio donde ocurrió el hecho", respuesta.getBody().error());
    }

    @Test
    @DisplayName("Un reporte inexistente responde 404 con el mensaje del dominio")
    void noEncontradoEs404() {
        // Arrange
        ManejadorErrores manejador = new ManejadorErrores();
        ReporteNoEncontradoException excepcion = new ReporteNoEncontradoException("PF-999999");

        // Act
        ResponseEntity<CuerpoError> respuesta = manejador.noEncontrado(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals(excepcion.getMessage(), respuesta.getBody().error());
    }

    @Test
    @DisplayName("Una operación no permitida responde 409 con el mensaje del dominio")
    void operacionNoPermitidaEs409() {
        // Arrange
        ManejadorErrores manejador = new ManejadorErrores();

        // Act
        ResponseEntity<CuerpoError> respuesta = manejador.operacionNoPermitida(
                new OperacionNoPermitidaException("El reporte PF-001 ya no admite avistamientos"));

        // Assert
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("El reporte PF-001 ya no admite avistamientos", respuesta.getBody().error());
    }

    @Test
    @DisplayName("Un JSON ilegible responde 400 sin mostrar el detalle técnico")
    void cuerpoIlegibleEs400() {
        // Arrange
        ManejadorErrores manejador = new ManejadorErrores();
        HttpMessageNotReadableException excepcion = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character", new MockHttpInputMessage(new byte[0]));

        // Act
        ResponseEntity<CuerpoError> respuesta = manejador.cuerpoIlegible(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El cuerpo de la petición no es válido", respuesta.getBody().error());
    }
}
