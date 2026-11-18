package sv.edu.udb.rbo.model;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Error de base de datos ya clasificado y con un mensaje listo para mostrar al usuario.
 *
 * <p>El DataManager atrapa la {@link SQLException} que lanzan las clases Peer y la convierte
 * en esta excepción con {@link #desde(SQLException, String)}. Así la traducción de los códigos
 * de MariaDB/MySQL vive en un solo lugar, y los controladores solo hacen
 * {@code request.setAttribute("error", e.getMessage())}.</p>
 *
 * <p>Para soportar un error nuevo: agregue un valor a {@link Tipo} y un caso en
 * {@link #clasificar(SQLException)}.</p>
 */
public class BaseDatosException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Categorías de error, de la más específica a la más general. */
    public enum Tipo {
        /** El registro tiene filas que dependen de él (llave foránea). Código 1451. */
        REGISTRO_EN_USO,
        /** Llave primaria o única repetida. Código 1062. */
        DUPLICADO,
        /** El valor no cabe o no es válido para la columna. Códigos 1048, 1265, 1292, 1366, 1406. */
        VALOR_INVALIDO,
        /** El registro no existe (no viene de una SQLException: 0 filas afectadas). */
        NO_ENCONTRADO,
        /** No hay conexión con el servidor o el pool. SQLState 08xxx. */
        SIN_CONEXION,
        /** Tabla bloqueada por otra operación o deadlock. Códigos 1205 y 1213. */
        BLOQUEO,
        /** Cualquier otro error. */
        OTRO
    }

    /** Código MariaDB: Cannot delete or update a parent row: a foreign key constraint fails. */
    private static final int ER_ROW_IS_REFERENCED = 1451;
    private static final int ER_ROW_IS_REFERENCED_OLD = 1217;
    private static final int ER_DUP_ENTRY = 1062;
    private static final int ER_LOCK_WAIT_TIMEOUT = 1205;
    private static final int ER_LOCK_DEADLOCK = 1213;
    private static final int ER_BAD_NULL = 1048;
    private static final int WARN_DATA_TRUNCATED = 1265;
    private static final int ER_TRUNCATED_WRONG_VALUE = 1292;
    private static final int ER_TRUNCATED_WRONG_VALUE_FOR_FIELD = 1366;
    private static final int ER_DATA_TOO_LONG = 1406;

    /** Extrae la tabla hija del mensaje de 1451: "... fails (`rbo_marcas`.`marca`, CONSTRAINT ...". */
    private static final Pattern TABLA_HIJA = Pattern.compile("\\(`[^`]+`\\.`([^`]+)`");

    private final Tipo tipo;
    private final String detalleTecnico;

    public BaseDatosException(Tipo tipo, String mensaje, String detalleTecnico, Throwable causa) {
        super(mensaje, causa);
        this.tipo = tipo;
        this.detalleTecnico = detalleTecnico;
    }

    public BaseDatosException(Tipo tipo, String mensaje) {
        this(tipo, mensaje, null, null);
    }

    public Tipo getTipo() {
        return tipo;
    }

    /** Mensaje original del driver (útil para el administrador o para el log). Puede ser null. */
    public String getDetalleTecnico() {
        return detalleTecnico;
    }

    /**
     * Convierte una SQLException en un error clasificado.
     *
     * @param e         la excepción del driver.
     * @param operacion lo que se intentaba hacer, para el mensaje (por ejemplo "eliminar el empleado mm263663").
     */
    public static BaseDatosException desde(SQLException e, String operacion) {
        Tipo tipo = clasificar(e);
        String mensaje;
        switch (tipo) {
            case REGISTRO_EN_USO:
                String tabla = tablaHija(e.getMessage());
                mensaje = "No se puede " + operacion + " porque tiene registros asociados"
                        + (tabla != null ? " en la tabla \"" + tabla + "\"" : "")
                        + ". Elimine o reasigne esos registros primero.";
                break;
            case DUPLICADO:
                mensaje = "No se puede " + operacion + ": ya existe un registro con ese identificador.";
                break;
            case VALOR_INVALIDO:
                mensaje = "No se puede " + operacion + ": uno de los valores no es válido o es demasiado largo.";
                break;
            case SIN_CONEXION:
                mensaje = "No hay conexión con la base de datos. Encienda MySQL en XAMPP e intente de nuevo.";
                break;
            case BLOQUEO:
                mensaje = "La base de datos está ocupada con otra operación. Intente de nuevo en unos segundos.";
                break;
            case OTRO:
            default:
                mensaje = "Ocurrió un error inesperado al " + operacion + ".";
                break;
        }
        return new BaseDatosException(tipo, mensaje, e.getMessage(), e);
    }

    /** Decide el tipo revisando la excepción y sus causas (el pool suele envolver la original). */
    static Tipo clasificar(SQLException e) {
        Throwable actual = e;
        while (actual != null) {
            if (actual instanceof SQLException) {
                SQLException sql = (SQLException) actual;
                switch (sql.getErrorCode()) {
                    case ER_ROW_IS_REFERENCED:
                    case ER_ROW_IS_REFERENCED_OLD:
                        return Tipo.REGISTRO_EN_USO;
                    case ER_DUP_ENTRY:
                        return Tipo.DUPLICADO;
                    case ER_LOCK_WAIT_TIMEOUT:
                    case ER_LOCK_DEADLOCK:
                        return Tipo.BLOQUEO;
                    case ER_BAD_NULL:
                    case WARN_DATA_TRUNCATED:
                    case ER_TRUNCATED_WRONG_VALUE:
                    case ER_TRUNCATED_WRONG_VALUE_FOR_FIELD:
                    case ER_DATA_TOO_LONG:
                        return Tipo.VALOR_INVALIDO;
                    default:
                        break;
                }
                String estado = sql.getSQLState();
                if (estado != null && estado.startsWith("08")) {
                    return Tipo.SIN_CONEXION;
                }
            }
            actual = actual.getCause();
        }
        return Tipo.OTRO;
    }

    private static String tablaHija(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        Matcher m = TABLA_HIJA.matcher(mensaje);
        return m.find() ? m.group(1) : null;
    }
}
