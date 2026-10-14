package inmuebles.dominio.modelo;

import java.time.LocalDate;

public final class Apartamento extends Inmueble {
    private final int numeroPiso;

    public Apartamento(String chip, LocalDate fechaCompra, String direccion, EstadoInmueble estado,
                       Propietario propietario, int numeroPiso) {
        super(chip, fechaCompra, direccion, estado, propietario);
        if (numeroPiso <= 0) {
            throw new IllegalArgumentException("El número de piso debe ser mayor que cero.");
        }
        this.numeroPiso = numeroPiso;
    }

    public int getNumeroPiso() { return numeroPiso; }

    @Override
    public String getTipo() { return "APARTAMENTO"; }
}