package sv.edu.udb.rbo.beans;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * JavaBean que representa una fila de la tabla Tarifa (la tarifa de un empleado para una
 * fecha y un horario). Lo usa la Interfaz de RH.
 *
 * <p>{@code nombreEmpleado} no es una columna de Tarifa: se llena con un JOIN a Empleado al
 * listar el historial, para mostrar el nombre junto al carnet.</p>
 */
public class Tarifa implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idTarifa;
    private String idEmpleado = "";
    private String nombreEmpleado = "";
    private Date fechaTarifa;
    private Time horaEntrada;
    private Time horaSalida;
    private double tarifa;

    public Tarifa() {
    }

    public Tarifa(String idEmpleado, Date fechaTarifa, Time horaEntrada, Time horaSalida, double tarifa) {
        this.idEmpleado = idEmpleado;
        this.fechaTarifa = fechaTarifa;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.tarifa = tarifa;
    }

    public int getIdTarifa() {
        return idTarifa;
    }

    public void setIdTarifa(int idTarifa) {
        this.idTarifa = idTarifa;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public Date getFechaTarifa() {
        return fechaTarifa;
    }

    public void setFechaTarifa(Date fechaTarifa) {
        this.fechaTarifa = fechaTarifa;
    }

    public Time getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(Time horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public Time getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(Time horaSalida) {
        this.horaSalida = horaSalida;
    }

    public double getTarifa() {
        return tarifa;
    }

    public void setTarifa(double tarifa) {
        this.tarifa = tarifa;
    }
}
