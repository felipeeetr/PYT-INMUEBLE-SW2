package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.InmuebleDto;

public interface ActualizarInmueble {
    InmuebleDto ejecutar(InmuebleDto datos);
}