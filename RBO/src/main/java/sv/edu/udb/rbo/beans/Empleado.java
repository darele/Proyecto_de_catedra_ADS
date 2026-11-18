package sv.edu.udb.rbo.beans;

import java.io.Serializable;
import java.sql.Time;

/**
 * JavaBean que representa una fila de la tabla Empleado.
 *
 * <p>Sigue la convención del libro (Capítulos 4 y 6): campos privados, constructor sin
 * argumentos y un par get/set por propiedad, para que EL lo lea con
 * {@code ${empleado.nombre}}. Se guarda en sesión al iniciar sesión, por eso implementa
 * {@link Serializable}.</p>
 *
 * <p>No incluye la contraseña: el hash nunca sale de la capa de modelo.</p>
 */
public class Empleado implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Valor del ENUM rol que da acceso a la interfaz de administración. */
    public static final String ROL_ADMIN = "admin";
    /** Valor del ENUM rol que da acceso a la Interfaz de RH. */
    public static final String ROL_RH = "RH";

    private String idEmpleado = "";
    private String nombre = "";
    private String apellido = "";
    private Double tarifa;
    private Time horaEntrada;
    private Time horaSalida;
    private int tiempoReceso;
    private String rol = ""; // admin | RH | Contabilidad | Empleado

    public Empleado() {
    }

    public Empleado(String idEmpleado, String nombre, String apellido, String rol) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
    }

    public Empleado(String idEmpleado,
                    String nombre,
                    String apellido,
                    Double tarifa,
                    Time horaEntrada,
                    Time horaSalida,
                    int tiempoReceso,
                    String rol) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tarifa = tarifa;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.tiempoReceso = tiempoReceso;
        this.rol = rol;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Double getTarifa() {
        return tarifa;
    }

    public void setTarifa(Double tarifa) {
        this.tarifa = tarifa;
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

    public int getTiempoReceso() {
        return tiempoReceso;
    }

    public void setTiempoReceso(int tiempoReceso) {
        this.tiempoReceso = tiempoReceso;
    }

    /** Propiedad derivada de solo lectura, usada en las vistas como {@code ${empleado.nombreCompleto}}. */
    public String getNombreCompleto() {
        return (nombre + " " + apellido).trim();
    }

    /** Propiedad derivada de solo lectura: {@code ${empleado.admin}}. */
    public boolean isAdmin() {
        return ROL_ADMIN.equalsIgnoreCase(rol);
    }

    /** Propiedad derivada de solo lectura: {@code ${empleado.RH}}. */
    public boolean isRH() {
        return ROL_RH.equalsIgnoreCase(rol);
    }
}
