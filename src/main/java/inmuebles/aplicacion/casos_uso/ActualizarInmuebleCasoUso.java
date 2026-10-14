package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.entrada.ActualizarInmueble;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;

public final class ActualizarInmuebleCasoUso implements ActualizarInmueble {
    private final RepositorioInmuebles inmuebles;
    private final RepositorioPropietarios propietarios;

    public ActualizarInmuebleCasoUso(RepositorioInmuebles inmuebles, RepositorioPropietarios propietarios) {
        this.inmuebles = inmuebles;
        this.propietarios = propietarios;
    }

    @Override
    public InmuebleDto ejecutar(InmuebleDto datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del inmueble son obligatorios.");
        }
        if (datos.chip() == null || datos.chip().isBlank()) {
            throw new IllegalArgumentException("El CHIP es obligatorio.");
        }
        if (!inmuebles.existePorChip(datos.chip())) {
            throw new RecursoNoEncontradoException("No existe un inmueble con el CHIP indicado.");
        }
        Propietario propietario = propietarios.buscarPorIdentificacion(datos.identificacionPropietario())
                .orElseThrow(() -> new RecursoNoEncontradoException("El propietario seleccionado no existe."));
        Inmueble inmueble = MapeadorInmuebles.desdeDto(datos, propietario);
        inmuebles.actualizar(inmueble);
        return MapeadorInmuebles.aDto(inmueble);
    }
}