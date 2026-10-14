package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.entrada.ConsultarInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import java.util.List;

public final class ConsultarInmueblesCasoUso implements ConsultarInmuebles {
    private final RepositorioInmuebles inmuebles;

    public ConsultarInmueblesCasoUso(RepositorioInmuebles inmuebles) { this.inmuebles = inmuebles; }

    @Override
    public List<InmuebleDto> ejecutar() {
        return inmuebles.listar().stream().map(MapeadorInmuebles::aDto).toList();
    }
}