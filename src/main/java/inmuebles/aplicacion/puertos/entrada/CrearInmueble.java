package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.InmuebleDto;

public interface CrearInmueble {
    InmuebleDto ejecutar(InmuebleDto datos);
}