package inmuebles.adaptadores.entrada.javafx;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.dto.PropietarioDto;
import inmuebles.aplicacion.puertos.entrada.ActualizarInmueble;
import inmuebles.aplicacion.puertos.entrada.ConsultarInmuebles;
import inmuebles.aplicacion.puertos.entrada.CrearInmueble;
import inmuebles.aplicacion.puertos.entrada.EliminarInmueble;
import inmuebles.aplicacion.puertos.entrada.ListarPropietarios;

import java.util.List;

public final class ControladorInmuebles {
    private final CrearInmueble crear;
    private final ConsultarInmuebles consultar;
    private final ActualizarInmueble actualizar;
    private final EliminarInmueble eliminar;
    private final ListarPropietarios listarPropietarios;

    public ControladorInmuebles(CrearInmueble crear, ConsultarInmuebles consultar,
                                ActualizarInmueble actualizar, EliminarInmueble eliminar,
                                ListarPropietarios listarPropietarios) {
        this.crear = crear;
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.eliminar = eliminar;
        this.listarPropietarios = listarPropietarios;
    }

    public List<InmuebleDto> consultarInmuebles() { return consultar.ejecutar(); }
    public List<PropietarioDto> listarPropietarios() { return listarPropietarios.ejecutar(); }
    public InmuebleDto crear(InmuebleDto datos) { return crear.ejecutar(datos); }
    public InmuebleDto actualizar(InmuebleDto datos) { return actualizar.ejecutar(datos); }
    public void eliminar(String chip) { eliminar.ejecutar(chip); }
}