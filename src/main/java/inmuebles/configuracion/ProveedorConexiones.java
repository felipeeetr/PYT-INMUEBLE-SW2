package inmuebles.configuracion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProveedorConexiones {
    private static final ProveedorConexiones INSTANCIA = new ProveedorConexiones();
    private volatile String url;
    private volatile String usuario = "sa";
    private volatile String clave = "";

    private ProveedorConexiones() {
        this.url = System.getProperty("inmuebles.db.url", "jdbc:h2:file:./data/inmuebles;DB_CLOSE_ON_EXIT=FALSE");
    }

    public static ProveedorConexiones obtenerInstancia() { return INSTANCIA; }

    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public void prepararAlmacen() throws IOException {
        String prefijo = "jdbc:h2:file:";
        if (url.startsWith(prefijo)) {
            String ruta = url.substring(prefijo.length()).split(";", 2)[0];
            Path archivo = Path.of(ruta).toAbsolutePath();
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
        }
    }

    public synchronized void configurar(String nuevaUrl, String nuevoUsuario, String nuevaClave) {
        if (nuevaUrl == null || nuevaUrl.isBlank()) {
            throw new IllegalArgumentException("La URL de la base de datos es obligatoria.");
        }
        url = nuevaUrl;
        usuario = nuevoUsuario == null ? "sa" : nuevoUsuario;
        clave = nuevaClave == null ? "" : nuevaClave;
    }
}