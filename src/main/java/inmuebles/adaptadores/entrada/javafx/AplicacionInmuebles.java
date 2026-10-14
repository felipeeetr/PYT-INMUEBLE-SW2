package inmuebles.adaptadores.entrada.javafx;

import inmuebles.aplicacion.casos_uso.ActualizarInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.ConsultarInmueblesCasoUso;
import inmuebles.aplicacion.casos_uso.CrearInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.EliminarInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.ListarPropietariosCasoUso;
import inmuebles.adaptadores.salida.persistencia.RepositorioInmueblesJdbc;
import inmuebles.adaptadores.salida.persistencia.RepositorioPropietariosJdbc;
import inmuebles.configuracion.InicializadorBaseDatos;
import inmuebles.configuracion.ProveedorConexiones;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class AplicacionInmuebles extends Application {
    @Override
    public void start(Stage escenario) {
        ProveedorConexiones conexiones = ProveedorConexiones.obtenerInstancia();
        InicializadorBaseDatos.inicializar(conexiones);
        RepositorioInmueblesJdbc inmuebles = new RepositorioInmueblesJdbc(conexiones);
        RepositorioPropietariosJdbc propietarios = new RepositorioPropietariosJdbc(conexiones);
        ControladorInmuebles controlador = new ControladorInmuebles(
                new CrearInmuebleCasoUso(inmuebles, propietarios),
                new ConsultarInmueblesCasoUso(inmuebles),
                new ActualizarInmuebleCasoUso(inmuebles, propietarios),
                new EliminarInmuebleCasoUso(inmuebles),
                new ListarPropietariosCasoUso(propietarios));
        VistaInmuebles vista = new VistaInmuebles(controlador);
        Scene escena = new Scene(vista.construir(), 1420, 800);
        var css = getClass().getResource("/estilos.css");
        if (css != null) {
            escena.getStylesheets().add(css.toExternalForm());
        }
        escenario.setTitle("Gestión de Inmuebles");
        escenario.setMinWidth(1050);
        escenario.setMinHeight(650);
        escenario.setScene(escena);
        escenario.show();
    }

    public static void main(String[] args) { launch(args); }
}