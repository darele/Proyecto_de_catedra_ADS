package sv.edu.udb.rbo.model;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import sv.edu.udb.rbo.beans.Empleado;
import sv.edu.udb.rbo.beans.Marca;
import sv.edu.udb.rbo.beans.ResumenAdmin;

/**
 * Clase central del modelo (patrón DataManager + Peer del libro, Capítulo 5).
 *
 * <ul>
 *   <li>Es el ÚNICO punto de entrada a la base de datos y oculta el SQL al resto de la
 *       aplicación. Sus métodos reciben y devuelven JavaBeans. Nadie llama a las clases
 *       *Peer directamente. En el eshop del libro lo llaman las JSP con jsp:useBean; aquí lo
 *       llaman los controladores (Model 2 del Capítulo 3), para que las vistas no tengan lógica.</li>
 *   <li>Abre las conexiones y delega el SQL de cada tabla a su clase Peer
 *       (EmpleadoPeer, MarcaPeer, EsquemaPeer).</li>
 *   <li>Hay una sola instancia por aplicación, guardada en el ámbito application con el
 *       nombre {@value #ATRIBUTO} (equivale a
 *       {@code <jsp:useBean id="dataManager" scope="application"/>}).</li>
 *   <li>La conexión sale de un pool JNDI (Capítulos 9 y 10) definido en
 *       META-INF/context.xml, no de usuario y contraseña escritos en el código.</li>
 * </ul>
 */
public class DataManager {

    /** Nombre del atributo en el ServletContext. */
    public static final String ATRIBUTO = "dataManager";

    /** Nombre JNDI del pool de conexiones, declarado en context.xml y en web.xml (resource-ref). */
    public static final String RECURSO_JNDI = "jdbc/rbo";

    private static final Logger LOG = Logger.getLogger(DataManager.class.getName());

    private DataSource dataSource;
    private volatile boolean esquemaListo;

    /**
     * Devuelve el DataManager de la aplicación y lo crea la primera vez
     * (la misma semántica que jsp:useBean con scope="application").
     */
    public static DataManager desde(ServletContext context) {
        synchronized (DataManager.class) {
            DataManager dataManager = (DataManager) context.getAttribute(ATRIBUTO);
            if (dataManager == null) {
                dataManager = new DataManager();
                context.setAttribute(ATRIBUTO, dataManager);
            }
            return dataManager;
        }
    }

    // ------------------------------------------------------------------ conexiones

    /**
     * Toma una conexión del pool. Quien la pide debe cerrarla, de preferencia con
     * try-with-resources: al cerrarla vuelve al pool (es el {@code putConnection} del libro).
     */
    public Connection getConnection() throws SQLException {
        return obtenerDataSource().getConnection();
    }

    private synchronized DataSource obtenerDataSource() throws SQLException {
        if (dataSource == null) {
            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:comp/env");
                dataSource = (DataSource) envContext.lookup(RECURSO_JNDI);
            } catch (NamingException e) {
                throw new SQLException("No se encontró el recurso JNDI java:comp/env/" + RECURSO_JNDI
                        + ". Revise META-INF/context.xml y el resource-ref de web.xml.", e);
            }
        }
        return dataSource;
    }

    /**
     * Libera el driver JDBC al detener o redesplegar la aplicación. Como el driver va dentro
     * del WAR (WEB-INF/lib), si no se des-registra Tomcat avisa de una fuga de memoria
     * ("failed to unregister it" / "mysql-cj-abandoned-connection-cleanup").
     * Lo llama RboServlet.destroy().
     */
    public void cerrar() {
        ClassLoader cargadorApp = DataManager.class.getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cargadorApp) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "No se pudo des-registrar el driver {0}", driver);
                }
            }
        }
        AbandonedConnectionCleanupThread.checkedShutdown();
    }

    // ------------------------------------------------------------------ esquema

    /**
     * Crea las tablas si todavía no existen (ejecuta rbo_create.sql). Solo trabaja la
     * primera vez que tiene éxito; las llamadas siguientes no hacen nada.
     */
    public void asegurarEsquema() throws SQLException {
        if (!esquemaListo) {
            synchronized (this) {
                if (!esquemaListo) {
                    EsquemaPeer.crearTablas(this);
                    esquemaListo = true;
                    LOG.info("Esquema de la base de datos verificado.");
                }
            }
        }
    }

    /**
     * Como {@link #asegurarEsquema()}, pero si falla solo deja el aviso en el log.
     * Un error del script (por ejemplo, una tabla importada con otra configuración) no debe
     * impedir el login ni las consultas que sí funcionan. Si la base de datos está caída,
     * el error aparece igual en la consulta siguiente.
     */
    private void intentarAsegurarEsquema() {
        try {
            asegurarEsquema();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo verificar el esquema (se reintentará): {0}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ empleados

    /**
     * Valida carnet y contraseña.
     *
     * @return el empleado, o {@code null} si las credenciales no coinciden.
     * @throws SQLException si la base de datos no responde.
     * @throws IllegalStateException si falta configurar la SALT (secret.properties).
     */
    public Empleado validarCredenciales(String carnet, String passwordPlano) throws SQLException {
        intentarAsegurarEsquema();
        String hash = SeguridadUtil.encriptarSHA256(passwordPlano);
        return EmpleadoPeer.buscarPorCredenciales(this, carnet, hash);
    }

    public List<Empleado> getEmpleados() throws SQLException {
        return EmpleadoPeer.listarTodos(this);
    }

    public void registrarEmpleado(Empleado empleado, String passwordPlano) throws SQLException{
        intentarAsegurarEsquema();
        String hash = SeguridadUtil.encriptarSHA256(passwordPlano);
        EmpleadoPeer.insertar(this, empleado, hash);
    }

    /**
     * Elimina un empleado.
     *
     * <p>Aquí se concentra el manejo de errores: la SQLException del Peer se convierte en una
     * {@link BaseDatosException} con tipo y mensaje para el usuario. Las validaciones extra
     * (por ejemplo, borrar en cascada sus marcas, o impedir borrar al último admin) van aquí,
     * antes de llamar al Peer.</p>
     *
     * @throws BaseDatosException con tipo NO_ENCONTRADO si el carnet no existe, REGISTRO_EN_USO
     *         si tiene marcas o tarifas, SIN_CONEXION si la base está caída, etc.
     */
    public void eliminarEmpleado(String idEmpleado) throws BaseDatosException {
        String operacion = "eliminar el empleado " + idEmpleado;
        int filas;
        try {
            filas = EmpleadoPeer.eliminar(this, idEmpleado);
        } catch (SQLException e) {
            BaseDatosException error = BaseDatosException.desde(e, operacion);
            LOG.log(Level.WARNING, "Error al {0} [{1}]: {2}",
                    new Object[]{operacion, error.getTipo(), e.getMessage()});
            throw error;
        }
        if (filas == 0) {
            throw new BaseDatosException(BaseDatosException.Tipo.NO_ENCONTRADO,
                    "No existe un empleado con el carnet " + idEmpleado + ". Puede que ya se haya eliminado.");
        }
    }

    // ------------------------------------------------------------------ marcas

    public void registrarMarca(Marca marca) throws SQLException {
        MarcaPeer.insertarMarca(this, marca);
    }

    // ------------------------------------------------------------------ administración

    /**
     * Datos de la portada de administración. Nunca lanza excepción: si la base de datos
     * falla, devuelve un resumen con {@code baseDisponible=false}.
     */
    public ResumenAdmin getResumenAdmin() {
        intentarAsegurarEsquema();
        try {
            ResumenAdmin resumen = new ResumenAdmin();
            resumen.setTotalEmpleados(EmpleadoPeer.contar(this));
            resumen.setMarcasHoy(MarcaPeer.contarDeHoy(this));
            resumen.setBaseDisponible(true);
            return resumen;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo leer el resumen de administración: {0}", e.getMessage());
            return ResumenAdmin.sinBase();
        }
    }
}
