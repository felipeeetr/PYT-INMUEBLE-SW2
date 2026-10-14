package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.excepciones.ConflictoException;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasosUsoInmueblesTest {
    private final RepositorioFalso inmuebles = new RepositorioFalso();
    private final Propietario propietario = new Propietario("P-1", "Ana Torres", "ana@example.com");
    private final RepositorioPropietarios propietarios = new PropietariosFalso(propietario);
    private CrearInmuebleCasoUso crear;
    private ConsultarInmueblesCasoUso consultar;
    private ActualizarInmuebleCasoUso actualizar;
    private EliminarInmuebleCasoUso eliminar;

    @BeforeEach
    void preparar() {
        inmuebles.datos.clear();
        inmuebles.fallarAlGuardar = false;
        crear = new CrearInmuebleCasoUso(inmuebles, propietarios);
        consultar = new ConsultarInmueblesCasoUso(inmuebles);
        actualizar = new ActualizarInmuebleCasoUso(inmuebles, propietarios);
        eliminar = new EliminarInmuebleCasoUso(inmuebles);
    }

    @Test
    void creaConsultaActualizaDeCasaAApartamentoYElimina() {
        InmuebleDto casa = datos("CHIP-1", "CASA", 2, new BigDecimal("24.50"), TipoUso.BODEGA);
        crear.ejecutar(casa);
        assertEquals("CASA", consultar.ejecutar().getFirst().tipo());

        InmuebleDto apartamento = new InmuebleDto("CHIP-1", casa.fechaCompra(), casa.direccion(),
                EstadoInmueble.ARRENDADO, "APARTAMENTO", "P-1", null, 5, null, null, null);
        actualizar.ejecutar(apartamento);
        InmuebleDto resultado = consultar.ejecutar().getFirst();
        assertEquals("APARTAMENTO", resultado.tipo());
        assertEquals(5, resultado.numeroPiso());
        assertNull(resultado.areaM2());

        eliminar.ejecutar("CHIP-1");
        assertTrue(consultar.ejecutar().isEmpty());
    }

    @Test
    void rechazaChipDuplicadoYRecursosInexistentes() {
        crear.ejecutar(datos("CHIP-1", "APARTAMENTO", 3, null, null));
        assertThrows(ConflictoException.class,
                () -> crear.ejecutar(datos("CHIP-1", "APARTAMENTO", 4, null, null)));
        assertThrows(RecursoNoEncontradoException.class,
                () -> actualizar.ejecutar(datos("NO-EXISTE", "CASA", 1, null, null)));
        assertThrows(RecursoNoEncontradoException.class, () -> eliminar.ejecutar("NO-EXISTE"));
    }

    @Test
    void validaDatosDeDominioYPropietario() {
        InmuebleDto pisoInvalido = datos("CHIP-2", "APARTAMENTO", 0, null, null);
        assertThrows(IllegalArgumentException.class, () -> crear.ejecutar(pisoInvalido));

        InmuebleDto sinPropietario = new InmuebleDto("CHIP-3", LocalDate.now(), "Calle 1",
                EstadoInmueble.DISPONIBLE, "APARTAMENTO", "FALSO", "", 1, null, null, null);
        assertThrows(RecursoNoEncontradoException.class, () -> crear.ejecutar(sinPropietario));
        assertThrows(IllegalArgumentException.class,
                () -> new Sotano(new BigDecimal("0"), TipoUso.VIVIENDA));
        assertThrows(IllegalArgumentException.class,
                () -> new Casa("CHIP-X", LocalDate.now(), "Calle 2", EstadoInmueble.DISPONIBLE,
                        propietario, 0, null));
    }

    @Test
    void conservaLaCausaCuandoFallaElPuertoDeSalida() {
        inmuebles.fallarAlGuardar = true;
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> crear.ejecutar(datos("CHIP-4", "APARTAMENTO", 1, null, null)));
        assertEquals("fallo del repositorio", error.getMessage());
    }

    private InmuebleDto datos(String chip, String tipo, int piso, BigDecimal area, TipoUso uso) {
        return new InmuebleDto(chip, LocalDate.of(2024, 3, 10), "Calle 10 # 20-30",
                EstadoInmueble.DISPONIBLE, tipo, propietario.identificacion(), propietario.nombre(),
                tipo.equals("APARTAMENTO") ? piso : null, tipo.equals("CASA") ? piso : null, area, uso);
    }

    private static final class RepositorioFalso implements RepositorioInmuebles {
        private final Map<String, Inmueble> datos = new HashMap<>();
        private boolean fallarAlGuardar;

        @Override
        public void guardar(Inmueble inmueble) {
            if (fallarAlGuardar) throw new IllegalStateException("fallo del repositorio");
            datos.put(inmueble.getChip(), inmueble);
        }

        @Override
        public void actualizar(Inmueble inmueble) { datos.put(inmueble.getChip(), inmueble); }

        @Override
        public void eliminar(String chip) { datos.remove(chip); }

        @Override
        public List<Inmueble> listar() { return new ArrayList<>(datos.values()); }

        @Override
        public Optional<Inmueble> buscarPorChip(String chip) { return Optional.ofNullable(datos.get(chip)); }

        @Override
        public boolean existePorChip(String chip) { return datos.containsKey(chip); }
    }

    private static final class PropietariosFalso implements RepositorioPropietarios {
        private final Propietario propietario;

        private PropietariosFalso(Propietario propietario) { this.propietario = propietario; }

        @Override
        public List<Propietario> listar() { return List.of(propietario); }

        @Override
        public Optional<Propietario> buscarPorIdentificacion(String identificacion) {
            return propietario.identificacion().equals(identificacion) ? Optional.of(propietario) : Optional.empty();
        }
    }
}