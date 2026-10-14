package inmuebles.adaptadores.salida.persistencia;

import inmuebles.configuracion.InicializadorBaseDatos;
import inmuebles.configuracion.ProveedorConexiones;
import inmuebles.dominio.excepciones.ConflictoException;
import inmuebles.dominio.excepciones.PersistenciaException;
import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.Sotano;
import inmuebles.dominio.modelo.TipoUso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioInmueblesJdbcTest {
    private final ProveedorConexiones conexiones = ProveedorConexiones.obtenerInstancia();
    private RepositorioInmueblesJdbc inmuebles;
    private RepositorioPropietariosJdbc propietarios;

    @BeforeEach
    void prepararBaseSeparada() {
        conexiones.configurar("jdbc:h2:mem:pruebas_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1", "sa", "");
        InicializadorBaseDatos.inicializar(conexiones);
        inmuebles = new RepositorioInmueblesJdbc(conexiones);
        propietarios = new RepositorioPropietariosJdbc(conexiones);
    }

    @Test
    void persisteApartamentoCasaConSotanoYCasaSinSotano() {
        inmuebles.guardar(apartamento("A-1", 4));
        assertThrows(ConflictoException.class, () -> inmuebles.guardar(apartamento("A-1", 5)));
        inmuebles.guardar(casa("C-1", 2, new Sotano(new BigDecimal("18.75"), TipoUso.PARQUEADERO)));
        inmuebles.guardar(casa("C-2", 1, null));

        List<Inmueble> consultados = inmuebles.listar();
        assertEquals(3, consultados.size());
        assertInstanceOf(Apartamento.class, inmuebles.buscarPorChip("A-1").orElseThrow());
        Casa conSotano = assertInstanceOf(Casa.class, inmuebles.buscarPorChip("C-1").orElseThrow());
        assertEquals(new BigDecimal("18.75"), conSotano.getSotano().areaM2());
        Casa sinSotano = assertInstanceOf(Casa.class, inmuebles.buscarPorChip("C-2").orElseThrow());
        assertNull(sinSotano.getSotano());
    }

    @Test
    void convierteSubtiposYConservaChipConEliminacionDeSotano() {
        inmuebles.guardar(casa("CAMBIO-1", 3, new Sotano(new BigDecimal("20"), TipoUso.BODEGA)));
        inmuebles.actualizar(apartamento("CAMBIO-1", 7));
        Apartamento convertido = assertInstanceOf(Apartamento.class, inmuebles.buscarPorChip("CAMBIO-1").orElseThrow());
        assertEquals(7, convertido.getNumeroPiso());

        inmuebles.actualizar(casa("CAMBIO-1", 2, null));
        Casa casaActualizada = assertInstanceOf(Casa.class, inmuebles.buscarPorChip("CAMBIO-1").orElseThrow());
        assertNull(casaActualizada.getSotano());
        assertEquals("CAMBIO-1", casaActualizada.getChip());
    }

    @Test
    void rollbackRestauraApartamentoSiFallaInsercionDelNuevoSubtipo() throws SQLException {
        inmuebles.guardar(apartamento("ROLLBACK-1", 6));
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.createStatement()) {
            sentencia.execute("ALTER TABLE CASA ADD CONSTRAINT PISOS_PRUEBA CHECK (cantidad_pisos < 4)");
        }
        assertThrows(PersistenciaException.class, () -> inmuebles.actualizar(casa("ROLLBACK-1", 4, null)));

        Apartamento restaurado = assertInstanceOf(Apartamento.class,
                inmuebles.buscarPorChip("ROLLBACK-1").orElseThrow());
        assertEquals(6, restaurado.getNumeroPiso());
    }

    @Test
    void aplicaRestriccionesForaneasChecksCascadaYRestriccionDePropietario() throws SQLException {
        inmuebles.guardar(apartamento("FK-1", 2));
        assertThrows(SQLException.class, () -> propietariosEliminar("100000001"));

        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.createStatement()) {
            assertThrows(SQLException.class, () -> sentencia.executeUpdate(
                    "INSERT INTO APARTAMENTO (chip, numero_piso) VALUES ('NO-BASE', 1)"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate(
                    "INSERT INTO INMUEBLE (chip, fecha_compra, direccion, estado, tipo, propietario_id) "
                            + "VALUES ('CHECK-1', DATE '2024-01-01', 'Calle 1', 'INVALIDO', 'CASA', '100000001')"));
            sentencia.executeUpdate("DELETE FROM INMUEBLE WHERE chip = 'FK-1'");
        }
        assertFalse(inmuebles.existePorChip("FK-1"));
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.createStatement();
             var resultado = sentencia.executeQuery("SELECT COUNT(*) FROM APARTAMENTO WHERE chip = 'FK-1'")) {
            assertTrue(resultado.next());
            assertEquals(0, resultado.getInt(1));
        }
    }

    @Test
    void inicializacionDePropietariosEsIdempotente() {
        int cantidadInicial = propietarios.listar().size();
        InicializadorBaseDatos.inicializar(conexiones);
        assertEquals(cantidadInicial, propietarios.listar().size());
        assertNotNull(propietarios.buscarPorIdentificacion("100000001").orElse(null));
    }

        @Test
        void aplicaChecksDeTipoPisosSotanoYUso() throws SQLException {
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.createStatement()) {
            sentencia.executeUpdate("INSERT INTO INMUEBLE VALUES ('CHECK-A', DATE '2024-01-01', 'A', "
                + "'DISPONIBLE', 'APARTAMENTO', '100000001')");
            assertThrows(SQLException.class, () -> sentencia.executeUpdate(
                "INSERT INTO APARTAMENTO VALUES ('CHECK-A', 0)"));

            sentencia.executeUpdate("INSERT INTO INMUEBLE VALUES ('CHECK-C', DATE '2024-01-01', 'C', "
                + "'DISPONIBLE', 'CASA', '100000001')");
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("INSERT INTO CASA VALUES ('CHECK-C', 0)"));
            sentencia.executeUpdate("INSERT INTO INMUEBLE VALUES ('CHECK-S', DATE '2024-01-01', 'S', "
                + "'DISPONIBLE', 'CASA', '100000001')");
            sentencia.executeUpdate("INSERT INTO CASA VALUES ('CHECK-S', 1)");
            assertThrows(SQLException.class, () -> sentencia.executeUpdate(
                "INSERT INTO SOTANO VALUES ('CHECK-S', 0, 'BODEGA')"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate(
                "INSERT INTO SOTANO VALUES ('CHECK-S', 10, 'INVALIDO')"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("INSERT INTO INMUEBLE VALUES "
                + "('CHECK-T', DATE '2024-01-01', 'T', 'DISPONIBLE', 'INVALIDO', '100000001')"));
        }
        }

    private void propietariosEliminar(String identificacion) throws SQLException {
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.prepareStatement(
                "DELETE FROM PROPIETARIO WHERE identificacion = ?")) {
            sentencia.setString(1, identificacion);
            sentencia.executeUpdate();
        }
    }

    private static Apartamento apartamento(String chip, int piso) {
        return new Apartamento(chip, LocalDate.of(2023, 6, 15), "Carrera 10 # 5-20",
                EstadoInmueble.DISPONIBLE, propietario(), piso);
    }

    private static Casa casa(String chip, int pisos, Sotano sotano) {
        return new Casa(chip, LocalDate.of(2022, 2, 10), "Calle 20 # 30-40",
                EstadoInmueble.ARRENDADO, propietario(), pisos, sotano);
    }

    private static Propietario propietario() {
        return new Propietario("100000001", "Ana María Torres", "ana.torres@example.com");
    }
}