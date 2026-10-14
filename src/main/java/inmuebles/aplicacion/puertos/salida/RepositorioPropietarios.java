package inmuebles.aplicacion.puertos.salida;

import inmuebles.dominio.modelo.Propietario;
import java.util.List;
import java.util.Optional;

public interface RepositorioPropietarios {
    List<Propietario> listar();
    Optional<Propietario> buscarPorIdentificacion(String identificacion);
}