package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.PropietarioDto;
import inmuebles.aplicacion.puertos.entrada.ListarPropietarios;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import java.util.List;

public final class ListarPropietariosCasoUso implements ListarPropietarios {
    private final RepositorioPropietarios propietarios;

    public ListarPropietariosCasoUso(RepositorioPropietarios propietarios) { this.propietarios = propietarios; }

    @Override
    public List<PropietarioDto> ejecutar() {
        return propietarios.listar().stream()
                .map(p -> new PropietarioDto(p.identificacion(), p.nombre(), p.correo())).toList();
    }
}