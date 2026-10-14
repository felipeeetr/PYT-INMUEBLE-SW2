package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.InmuebleDto;
import java.util.List;

public interface ConsultarInmuebles {
    List<InmuebleDto> ejecutar();
}