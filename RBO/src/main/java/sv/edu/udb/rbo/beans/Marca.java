package sv.edu.udb.rbo.beans;

import java.io.Serializable;
import java.sql.Time;

/**
 * JavaBean que representa una fila de la tabla Marca.
 *
 * <p>{@code tipoMarca} usa los valores del ENUM de la base: entrada, receso o salida.
 * La fecha y la hora las pone la base con sus valores por defecto al insertar.</p>
 */
public class Marca implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Longitud máxima de la columna comentario (VARCHAR(100)). */
    public static final int MAX_COMENTARIO = 100;

    private int idMarca;
    private String idEmpleado = "";
    private String tipoMarca = "";
    private String comentario = "";
    private Time horaMarca = null;
    private Time fechaMarca = null;

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public int getIdMarca() {
        return idMarca;
    }


    public Time getHoraMarca() {
        return horaMarca;
    }

    public Time getFechaMarca() {
        return fechaMarca;
    }

    public void setFechaMarca(Time fechaMarca) {
        this.fechaMarca = fechaMarca;
    }

    public void setHoraMarca(Time horaMarca) {
        this.horaMarca = horaMarca;
    }

    public Marca() {
    }

    public Marca(String idEmpleado, String tipoMarca, String comentario) {
        this.idEmpleado = idEmpleado;
        this.tipoMarca = tipoMarca;
        setComentario(comentario);
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getTipoMarca() {
        return tipoMarca;
    }

    public void setTipoMarca(String tipoMarca) {
        this.tipoMarca = tipoMarca;
    }

    public String getComentario() {
        return comentario;
    }

    /** Recorta espacios y trunca al tamaño de la columna, para que el INSERT no falle. */
    public void setComentario(String comentario) {
        String limpio = (comentario == null) ? "" : comentario.trim();
        this.comentario = (limpio.length() > MAX_COMENTARIO) ? limpio.substring(0, MAX_COMENTARIO) : limpio;
    }
}
