package inmuebles.dominio.modelo;

import java.util.Objects;

public record Propietario(String identificacion, String nombre, String correo) {
    public Propietario {
        identificacion = requerido(identificacion, "La identificación del propietario es obligatoria.");
        nombre = requerido(nombre, "El nombre del propietario es obligatorio.");
        correo = requerido(correo, "El correo del propietario es obligatorio.");
    }

    private static String requerido(String valor, String mensaje) {
        Objects.requireNonNull(valor, mensaje);
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return limpio;
    }
}