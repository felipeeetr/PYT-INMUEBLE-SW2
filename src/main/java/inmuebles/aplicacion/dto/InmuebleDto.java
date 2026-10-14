package inmuebles.aplicacion.dto;

import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.TipoUso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InmuebleDto(
        String chip,
        LocalDate fechaCompra,
        String direccion,
        EstadoInmueble estado,
        String tipo,
        String identificacionPropietario,
        String nombrePropietario,
        Integer numeroPiso,
        Integer cantidadPisos,
        BigDecimal areaM2,
        TipoUso tipoUso) {
}