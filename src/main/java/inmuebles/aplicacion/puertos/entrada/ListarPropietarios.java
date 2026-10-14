package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.PropietarioDto;
import java.util.List;

public interface ListarPropietarios {
    List<PropietarioDto> ejecutar();
}