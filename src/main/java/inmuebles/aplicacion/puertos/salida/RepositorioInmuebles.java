package inmuebles.aplicacion.puertos.salida;

import inmuebles.dominio.modelo.Inmueble;
import java.util.List;
import java.util.Optional;

public interface RepositorioInmuebles {
    void guardar(Inmueble inmueble);
    void actualizar(Inmueble inmueble);
    void eliminar(String chip);
    List<Inmueble> listar();
    Optional<Inmueble> buscarPorChip(String chip);
    boolean existePorChip(String chip);
}