package sv.edu.udb.rbo;

import javax.servlet.ServletContext;
import sv.edu.udb.rbo.beans.Empleado;

/**
 * Interruptor del "modo desarrollo".
 *
 * <p>Con el modo activo, las Interfaces de Admin y de RH se abren sin pasar por el login con
 * un usuario de prueba, para desarrollarlas de forma desacoplada (sin usuarios en la
 * base ni SALT configurada).</p>
 *
 * <p>Se activa de dos formas:</p>
 * <ol>
 *   <li>Recomendada: la opción de JVM {@code -Drbo.modoDesarrollo=true} en la Run
 *       Configuration de IntelliJ. Es local de cada máquina y no se sube a git.</li>
 *   <li>El context-param {@code modoDesarrollo} de web.xml (viene en {@code false}).</li>
 * </ol>
 * <p>Nunca debe quedar activo en producción.</p>
 */
public final class ModoDesarrollo {

    /** Propiedad de sistema (opción de JVM {@code -D}). */
    public static final String PROPIEDAD = "rbo.modoDesarrollo";

    /** Nombre del context-param en web.xml. */
    public static final String PARAMETRO = "modoDesarrollo";

    private ModoDesarrollo() {
    }

    public static boolean estaActivo(ServletContext context) {
        return Boolean.getBoolean(PROPIEDAD)
                || Boolean.parseBoolean(context.getInitParameter(PARAMETRO));
    }

    /** Administrador ficticio de la sesión de prueba (no existe en la base de datos). */
    public static Empleado adminDePrueba() {
        return new Empleado("DEMO", "Admin", "de Prueba", Empleado.ROL_ADMIN);
    }

    /** Usuario de RH ficticio de la sesión de prueba de la Interfaz de RH. */
    public static Empleado rhDePrueba() {
        return new Empleado("DEMO-RH", "RH", "de Prueba", Empleado.ROL_RH);
    }
}
