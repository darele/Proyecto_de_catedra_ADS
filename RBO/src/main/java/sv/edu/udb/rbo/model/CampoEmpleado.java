package sv.edu.udb.rbo.model;

import java.sql.Time;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import sv.edu.udb.rbo.beans.Empleado;

/**
 * Campos de la tabla Empleado que se pueden editar uno por uno.
 *
 * <p>Es la "lista blanca" de columnas: en un PreparedStatement el nombre de la columna no se
 * puede pasar como {@code ?}, así que EmpleadoPeer solo acepta columnas que vengan de este
 * enum (nunca texto que mande el navegador).</p>
 *
 * <p>Cada valor sabe: el nombre del parámetro en el formulario, la columna en la base, una
 * etiqueta para mostrar, cómo convertir/validar el texto del formulario y cómo asignarse a
 * un {@link Empleado}. El carnet (llave primaria) y la contraseña no están a propósito.</p>
 */
public enum CampoEmpleado {

    NOMBRE("nombre", "nombre", "Nombre", Types.VARCHAR),
    APELLIDO("apellido", "Apellido", "Apellido", Types.VARCHAR),
    TARIFA("tarifa", "tarifa", "Tarifa por hora", Types.DOUBLE),
    HORA_ENTRADA("hora_entrada", "hora_entrada", "Hora de entrada", Types.TIME),
    HORA_SALIDA("hora_salida", "hora_salida", "Hora de salida", Types.TIME),
    TIEMPO_RECESO("tiempo_receso", "tiempo_receso", "Tiempo de receso", Types.INTEGER),
    ROL("rol", "rol", "Rol", Types.VARCHAR);

    /** Valores válidos del ENUM rol de la tabla. */
    public static final List<String> ROLES = Arrays.asList("admin", "RH", "Contabilidad", "Empleado");

    private static final int MAX_TEXTO = 30;

    private final String parametro;
    private final String columna;
    private final String etiqueta;
    private final int tipoSql;

    CampoEmpleado(String parametro, String columna, String etiqueta, int tipoSql) {
        this.parametro = parametro;
        this.columna = columna;
        this.etiqueta = etiqueta;
        this.tipoSql = tipoSql;
    }

    public String getParametro() {
        return parametro;
    }

    /** Nombre de la columna en la base. Solo lo usa EmpleadoPeer. */
    String getColumna() {
        return columna;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    int getTipoSql() {
        return tipoSql;
    }

    /** Busca el campo por el nombre del parámetro del formulario. Devuelve null si no existe. */
    public static CampoEmpleado desdeParametro(String parametro) {
        for (CampoEmpleado campo : values()) {
            if (campo.parametro.equals(parametro)) {
                return campo;
            }
        }
        return null;
    }

    /**
     * Convierte y valida el texto que llegó del formulario.
     *
     * @return String, Double (o null para tarifa vacía), Time o Integer según el campo.
     * @throws IllegalArgumentException con un mensaje para el usuario si el valor no es válido.
     */
    public Object convertir(String texto) {
        String valor = texto == null ? "" : texto.trim();
        switch (this) {
            case NOMBRE:
            case APELLIDO:
                if (valor.isEmpty()) {
                    throw new IllegalArgumentException(etiqueta + " no puede quedar vacío.");
                }
                if (valor.length() > MAX_TEXTO) {
                    throw new IllegalArgumentException(etiqueta + " admite máximo " + MAX_TEXTO + " caracteres.");
                }
                return valor;
            case TARIFA:
                if (valor.isEmpty()) {
                    return null; // la columna admite NULL
                }
                try {
                    double tarifa = Double.parseDouble(valor);
                    if (tarifa < 0) {
                        throw new IllegalArgumentException("La tarifa no puede ser negativa.");
                    }
                    return tarifa;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("La tarifa debe ser un número, por ejemplo 2.50.");
                }
            case HORA_ENTRADA:
            case HORA_SALIDA:
                if (valor.isEmpty()) {
                    throw new IllegalArgumentException(etiqueta + " es obligatoria.");
                }
                // <input type="time"> manda "HH:mm"; java.sql.Time necesita "HH:mm:ss".
                if (valor.length() == 5) {
                    valor += ":00";
                }
                try {
                    return Time.valueOf(valor);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(etiqueta + " no tiene un formato de hora válido.");
                }
            case TIEMPO_RECESO:
                try {
                    int minutos = Integer.parseInt(valor);
                    if (minutos < 0) {
                        throw new IllegalArgumentException("El tiempo de receso no puede ser negativo.");
                    }
                    return minutos;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El tiempo de receso debe ser un número entero de minutos.");
                }
            case ROL:
                if (!ROLES.contains(valor)) {
                    throw new IllegalArgumentException("El rol \"" + valor + "\" no es válido.");
                }
                return valor;
            default:
                throw new IllegalStateException("Campo sin conversión: " + this);
        }
    }

    /** Copia un valor ya convertido con {@link #convertir(String)} al bean. */
    public void asignar(Empleado empleado, Object valor) {
        switch (this) {
            case NOMBRE:
                empleado.setNombre((String) valor);
                break;
            case APELLIDO:
                empleado.setApellido((String) valor);
                break;
            case TARIFA:
                empleado.setTarifa((Double) valor);
                break;
            case HORA_ENTRADA:
                empleado.setHoraEntrada((Time) valor);
                break;
            case HORA_SALIDA:
                empleado.setHoraSalida((Time) valor);
                break;
            case TIEMPO_RECESO:
                empleado.setTiempoReceso((Integer) valor);
                break;
            case ROL:
                empleado.setRol((String) valor);
                break;
            default:
                throw new IllegalStateException("Campo sin asignación: " + this);
        }
    }
}
