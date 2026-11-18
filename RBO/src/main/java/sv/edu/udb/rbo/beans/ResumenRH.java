package sv.edu.udb.rbo.beans;

import java.io.Serializable;

/**
 * Datos que muestra la portada de la Interfaz de RH.
 *
 * <p>Igual que {@link ResumenAdmin}: si la base de datos no responde, {@code baseDisponible}
 * queda en {@code false} y la vista muestra un aviso en lugar de fallar.</p>
 */
public class ResumenRH implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean baseDisponible;
    private int totalEmpleados;
    private int totalTarifas;

    public ResumenRH() {
    }

    public static ResumenRH sinBase() {
        return new ResumenRH();
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

    public int getTotalTarifas() {
        return totalTarifas;
    }

    public void setTotalTarifas(int totalTarifas) {
        this.totalTarifas = totalTarifas;
    }
}
