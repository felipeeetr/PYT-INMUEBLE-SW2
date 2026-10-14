package inmuebles.configuracion;

import inmuebles.dominio.excepciones.PersistenciaException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class InicializadorBaseDatos {
    private static final List<String[]> PROPIETARIOS_PREDETERMINADOS = List.of(
            new String[]{"100000001", "Ana María Torres", "ana.torres@example.com"},
            new String[]{"100000002", "Carlos Andrés Ruiz", "carlos.ruiz@example.com"},
            new String[]{"100000003", "María Fernanda López", "maria.lopez@example.com"}
    );

    private InicializadorBaseDatos() { }

    public static void inicializar(ProveedorConexiones conexiones) {
        try {
            conexiones.prepararAlmacen();
            String esquema;
            try (var recurso = InicializadorBaseDatos.class.getResourceAsStream("/db/esquema.sql")) {
                if (recurso == null) {
                    throw new IOException("No se encontró /db/esquema.sql.");
                }
                esquema = new String(recurso.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            try (Connection conexion = conexiones.obtenerConexion(); Statement sentencia = conexion.createStatement()) {
                for (String sql : esquema.split(";")) {
                    if (!sql.isBlank()) {
                        sentencia.execute(sql);
                    }
                }
                cargarPropietarios(conexion);
            }
        } catch (IOException | SQLException e) {
            throw new PersistenciaException("No fue posible inicializar la base de datos.", e);
        }
    }

    private static void cargarPropietarios(Connection conexion) throws SQLException {
        String sql = "MERGE INTO PROPIETARIO (identificacion, nombre, correo) KEY (identificacion) VALUES (?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (String[] propietario : PROPIETARIOS_PREDETERMINADOS) {
                sentencia.setString(1, propietario[0]);
                sentencia.setString(2, propietario[1]);
                sentencia.setString(3, propietario[2]);
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }
}