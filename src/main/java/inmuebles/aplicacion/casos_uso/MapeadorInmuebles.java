package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.Sotano;
import inmuebles.dominio.modelo.TipoUso;

final class MapeadorInmuebles {
    private MapeadorInmuebles() { }

    static Inmueble desdeDto(InmuebleDto datos, Propietario propietario) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del inmueble son obligatorios.");
        }
        if (datos.estado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        if (datos.tipo() == null) {
            throw new IllegalArgumentException("El tipo de inmueble es obligatorio.");
        }
        return switch (datos.tipo().trim().toUpperCase()) {
            case "APARTAMENTO" -> new Apartamento(datos.chip(), datos.fechaCompra(), datos.direccion(),
                    datos.estado(), propietario, requerido(datos.numeroPiso(), "El número de piso es obligatorio."));
            case "CASA" -> new Casa(datos.chip(), datos.fechaCompra(), datos.direccion(), datos.estado(),
                    propietario, requerido(datos.cantidadPisos(), "La cantidad de pisos es obligatoria."),
                    crearSotano(datos));
            default -> throw new IllegalArgumentException("Tipo de inmueble no válido.");
        };
    }

    private static Sotano crearSotano(InmuebleDto datos) {
        if (datos.areaM2() == null && datos.tipoUso() == null) {
            return null;
        }
        if (datos.areaM2() == null || datos.tipoUso() == null) {
            throw new IllegalArgumentException("Indique tanto el área como el uso del sótano.");
        }
        return new Sotano(datos.areaM2(), datos.tipoUso());
    }

    private static int requerido(Integer valor, String mensaje) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    static InmuebleDto aDto(Inmueble inmueble) {
        Integer numeroPiso = null;
        Integer cantidadPisos = null;
        java.math.BigDecimal area = null;
        TipoUso uso = null;
        if (inmueble instanceof Apartamento apartamento) {
            numeroPiso = apartamento.getNumeroPiso();
        } else if (inmueble instanceof Casa casa) {
            cantidadPisos = casa.getCantidadPisos();
            if (casa.getSotano() != null) {
                area = casa.getSotano().areaM2();
                uso = casa.getSotano().tipoUso();
            }
        }
        Propietario propietario = inmueble.getPropietario();
        return new InmuebleDto(inmueble.getChip(), inmueble.getFechaCompra(), inmueble.getDireccion(),
                inmueble.getEstado(), inmueble.getTipo(), propietario.identificacion(), propietario.nombre(),
                numeroPiso, cantidadPisos, area, uso);
    }
}