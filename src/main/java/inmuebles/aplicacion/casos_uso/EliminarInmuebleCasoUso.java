package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.puertos.entrada.EliminarInmueble;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;

public final class EliminarInmuebleCasoUso implements EliminarInmueble {
    private final RepositorioInmuebles inmuebles;

    public EliminarInmuebleCasoUso(RepositorioInmuebles inmuebles) { this.inmuebles = inmuebles; }

    @Override
    public void ejecutar(String chip) {
        if (!inmuebles.existePorChip(chip)) {
            throw new RecursoNoEncontradoException("No existe un inmueble con el CHIP indicado.");
        }
        inmuebles.eliminar(chip);
    }
}