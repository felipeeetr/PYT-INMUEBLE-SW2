package inmuebles.aplicacion.dto;

public record PropietarioDto(String identificacion, String nombre, String correo) {
    @Override
    public String toString() { return nombre + " (" + identificacion + ")"; }
}