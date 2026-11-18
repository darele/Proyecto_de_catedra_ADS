package sv.edu.udb.rbo.beans;

import java.io.Serializable;

/**
 * Datos que muestra la portada de la Interfaz de Admin ("hola mundo").
 *
 * <p>Si la base de datos no responde, {@code baseDisponible} queda en {@code false} y la
 * vista muestra un aviso en lugar de fallar. Así la sección se puede abrir y probar
 * aunque MySQL esté apagado.</p>
 */
public class ResumenAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean baseDisponible;
    private int totalEmpleados;
    private int marcasHoy;

    public ResumenAdmin() {
    }

    public static ResumenAdmin sinBase() {
        return new ResumenAdmin();
    }

    public boolean isBaseDisponible() {
        return baseDisponible;
    }

    public void setBaseDisponible(boolean baseDisponible) {
        this.baseDisponible = baseDisponible;
    }

    public int getTotalEmpleados() {
        return totalEmpleados;
    }

    public void setTotalEmpleados(int totalEmpleados) {
        this.totalEmpleados = totalEmpleados;
    }

    public int getMarcasHoy() {
        return marcasHoy;
    }

    public void setMarcasHoy(int marcasHoy) {
        this.marcasHoy = marcasHoy;
    }
}
