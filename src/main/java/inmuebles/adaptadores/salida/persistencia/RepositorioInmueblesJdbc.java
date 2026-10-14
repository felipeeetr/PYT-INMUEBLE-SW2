package inmuebles.adaptadores.salida.persistencia;

import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.configuracion.ProveedorConexiones;
import inmuebles.dominio.excepciones.ConflictoException;
import inmuebles.dominio.excepciones.PersistenciaException;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;
import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.Sotano;
import inmuebles.dominio.modelo.TipoUso;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RepositorioInmueblesJdbc implements RepositorioInmuebles {
    private static final String CONSULTA_BASE = "SELECT i.chip, i.fecha_compra, i.direccion, i.estado, i.tipo, "
            + "p.identificacion AS propietario_id, p.nombre AS propietario_nombre, p.correo AS propietario_correo, "
            + "a.numero_piso, c.cantidad_pisos, s.area_m2, s.tipo_uso "
            + "FROM INMUEBLE i JOIN PROPIETARIO p ON p.identificacion = i.propietario_id "
            + "LEFT JOIN APARTAMENTO a ON a.chip = i.chip LEFT JOIN CASA c ON c.chip = i.chip "
            + "LEFT JOIN SOTANO s ON s.chip_casa = i.chip ";
    private final ProveedorConexiones conexiones;

    public RepositorioInmueblesJdbc(ProveedorConexiones conexiones) { this.conexiones = conexiones; }

    @Override
    public void guardar(Inmueble inmueble) {
        enTransaccion(conexion -> {
            String sql = "INSERT INTO INMUEBLE (chip, fecha_compra, direccion, estado, tipo, propietario_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                asignarDatosComunes(sentencia, inmueble);
                sentencia.executeUpdate();
            }
            insertarTipo(conexion, inmueble);
            return null;
        });
    }

    @Override
    public void actualizar(Inmueble inmueble) {
        enTransaccion(conexion -> {
            String sql = "UPDATE INMUEBLE SET fecha_compra = ?, direccion = ?, estado = ?, tipo = ?, propietario_id = ? "
                    + "WHERE chip = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setDate(1, Date.valueOf(inmueble.getFechaCompra()));
                sentencia.setString(2, inmueble.getDireccion());
                sentencia.setString(3, inmueble.getEstado().name());
                sentencia.setString(4, inmueble.getTipo());
                sentencia.setString(5, inmueble.getPropietario().identificacion());
                sentencia.setString(6, inmueble.getChip());
                if (sentencia.executeUpdate() == 0) {
                    throw new RecursoNoEncontradoException("No existe un inmueble con el CHIP indicado.");
                }
            }
            try (PreparedStatement apartamento = conexion.prepareStatement("DELETE FROM APARTAMENTO WHERE chip = ?");
                 PreparedStatement casa = conexion.prepareStatement("DELETE FROM CASA WHERE chip = ?")) {
                apartamento.setString(1, inmueble.getChip());
                apartamento.executeUpdate();
                casa.setString(1, inmueble.getChip());
                casa.executeUpdate();
            }
            insertarTipo(conexion, inmueble);
            return null;
        });
    }

    @Override
    public void eliminar(String chip) {
        String sql = "DELETE FROM INMUEBLE WHERE chip = ?";
        try (Connection conexion = conexiones.obtenerConexion(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, chip);
            if (sentencia.executeUpdate() == 0) {
                throw new RecursoNoEncontradoException("No existe un inmueble con el CHIP indicado.");
            }
        } catch (SQLException e) {
            throw traducir(e, "No fue posible eliminar el inmueble.");
        }
    }

    @Override
    public List<Inmueble> listar() {
        try (Connection conexion = conexiones.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(CONSULTA_BASE + "ORDER BY i.chip");
             ResultSet resultados = sentencia.executeQuery()) {
            List<Inmueble> inmuebles = new ArrayList<>();
            while (resultados.next()) {
                inmuebles.add(mapear(resultados));
            }
            return inmuebles;
        } catch (SQLException e) {
            throw traducir(e, "No fue posible consultar los inmuebles.");
        }
    }

    @Override
    public Optional<Inmueble> buscarPorChip(String chip) {
        try (Connection conexion = conexiones.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(CONSULTA_BASE + "WHERE i.chip = ?")) {
            sentencia.setString(1, chip);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? Optional.of(mapear(resultados)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir(e, "No fue posible consultar el inmueble.");
        }
    }

    @Override
    public boolean existePorChip(String chip) {
        String sql = "SELECT 1 FROM INMUEBLE WHERE chip = ?";
        try (Connection conexion = conexiones.obtenerConexion(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, chip);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next();
            }
        } catch (SQLException e) {
            throw traducir(e, "No fue posible verificar el CHIP.");
        }
    }

    private static void asignarDatosComunes(PreparedStatement sentencia, Inmueble inmueble) throws SQLException {
        sentencia.setString(1, inmueble.getChip());
        sentencia.setDate(2, Date.valueOf(inmueble.getFechaCompra()));
        sentencia.setString(3, inmueble.getDireccion());
        sentencia.setString(4, inmueble.getEstado().name());
        sentencia.setString(5, inmueble.getTipo());
        sentencia.setString(6, inmueble.getPropietario().identificacion());
    }

    private static void insertarTipo(Connection conexion, Inmueble inmueble) throws SQLException {
        if (inmueble instanceof Apartamento apartamento) {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    "INSERT INTO APARTAMENTO (chip, numero_piso) VALUES (?, ?)")) {
                sentencia.setString(1, inmueble.getChip());
                sentencia.setInt(2, apartamento.getNumeroPiso());
                sentencia.executeUpdate();
            }
        } else if (inmueble instanceof Casa casa) {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    "INSERT INTO CASA (chip, cantidad_pisos) VALUES (?, ?)")) {
                sentencia.setString(1, inmueble.getChip());
                sentencia.setInt(2, casa.getCantidadPisos());
                sentencia.executeUpdate();
            }
            if (casa.getSotano() != null) {
                try (PreparedStatement sentencia = conexion.prepareStatement(
                        "INSERT INTO SOTANO (chip_casa, area_m2, tipo_uso) VALUES (?, ?, ?)")) {
                    sentencia.setString(1, inmueble.getChip());
                    sentencia.setBigDecimal(2, casa.getSotano().areaM2());
                    sentencia.setString(3, casa.getSotano().tipoUso().name());
                    sentencia.executeUpdate();
                }
            }
        }
    }

    private static Inmueble mapear(ResultSet resultado) throws SQLException {
        Propietario propietario = new Propietario(resultado.getString("propietario_id"),
                resultado.getString("propietario_nombre"), resultado.getString("propietario_correo"));
        String chip = resultado.getString("chip");
        var fecha = resultado.getDate("fecha_compra").toLocalDate();
        String direccion = resultado.getString("direccion");
        EstadoInmueble estado = EstadoInmueble.valueOf(resultado.getString("estado"));
        if ("APARTAMENTO".equals(resultado.getString("tipo"))) {
            return new Apartamento(chip, fecha, direccion, estado, propietario, resultado.getInt("numero_piso"));
        }
        Sotano sotano = null;
        if (resultado.getBigDecimal("area_m2") != null) {
            sotano = new Sotano(resultado.getBigDecimal("area_m2"), TipoUso.valueOf(resultado.getString("tipo_uso")));
        }
        return new Casa(chip, fecha, direccion, estado, propietario, resultado.getInt("cantidad_pisos"), sotano);
    }

    private <T> T enTransaccion(Operacion<T> operacion) {
        try (Connection conexion = conexiones.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException falloRollback) {
                    e.addSuppressed(falloRollback);
                }
                if (e instanceof SQLException sql) {
                    throw traducir(sql, "La operación fue revertida por un error de persistencia.");
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw traducir(e, "No fue posible completar la transacción.");
        }
    }

    private static RuntimeException traducir(SQLException e, String mensaje) {
        if ("23505".equals(e.getSQLState())) {
            return new ConflictoException("Ya existe un inmueble con el CHIP indicado.", e);
        }
        return new PersistenciaException(mensaje, e);
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }
}