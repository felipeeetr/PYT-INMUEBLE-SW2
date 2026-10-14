package inmuebles.adaptadores.salida.persistencia;

import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.configuracion.ProveedorConexiones;
import inmuebles.dominio.excepciones.PersistenciaException;
import inmuebles.dominio.modelo.Propietario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RepositorioPropietariosJdbc implements RepositorioPropietarios {
    private final ProveedorConexiones conexiones;

    public RepositorioPropietariosJdbc(ProveedorConexiones conexiones) { this.conexiones = conexiones; }

    @Override
    public List<Propietario> listar() {
        String sql = "SELECT identificacion, nombre, correo FROM PROPIETARIO ORDER BY nombre";
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.prepareStatement(sql);
             var resultados = sentencia.executeQuery()) {
            List<Propietario> propietarios = new ArrayList<>();
            while (resultados.next()) {
                propietarios.add(new Propietario(resultados.getString("identificacion"),
                        resultados.getString("nombre"), resultados.getString("correo")));
            }
            return propietarios;
        } catch (SQLException e) {
            throw new PersistenciaException("No fue posible consultar los propietarios.", e);
        }
    }

    @Override
    public Optional<Propietario> buscarPorIdentificacion(String identificacion) {
        String sql = "SELECT identificacion, nombre, correo FROM PROPIETARIO WHERE identificacion = ?";
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, identificacion);
            try (var resultados = sentencia.executeQuery()) {
                if (!resultados.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Propietario(resultados.getString("identificacion"),
                        resultados.getString("nombre"), resultados.getString("correo")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No fue posible consultar el propietario.", e);
        }
    }
}