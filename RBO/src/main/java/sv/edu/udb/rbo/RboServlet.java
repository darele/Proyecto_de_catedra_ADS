package sv.edu.udb.rbo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sv.edu.udb.rbo.beans.AccionMarca;
import sv.edu.udb.rbo.beans.Empleado;
import sv.edu.udb.rbo.beans.EstadoMarca;
import sv.edu.udb.rbo.beans.Marca;
import sv.edu.udb.rbo.model.DataManager;
import sv.edu.udb.rbo.model.MarcaPeer;

/**
 * Controlador (la "C" de MVC) de la interfaz de empleado: login, logout y marca de asistencia.
 *
 * <p>Basado en el ShopServlet del libro:</p>
 * <ul>
 *   <li>{@code doGet} delega en {@code doPost}, y el parámetro {@code action} decide qué
 *       vista JSP mostrar. Sin action, o con un action desconocido, muestra la página por
 *       defecto (Capítulos 5 y 10, Listing 10-2).</li>
 *   <li>Se declara y mapea en WEB-INF/web.xml en la ruta {@code /rbo}, como en eshopx
 *       (Capítulos 7 y 10). El Capítulo 5 usa @WebServlet; aquí se dejó todo en web.xml.</li>
 *   <li>A diferencia de ShopServlet, que solo despacha y deja que las JSP pidan los datos al
 *       DataManager, este controlador sigue el Model 2 del Capítulo 3 (Fig. 3-2): pide los
 *       datos al modelo, los deja en el request o en la sesión y hace forward a la vista.
 *       Así las vistas no tienen lógica.</li>
 * </ul>
 *
 * <p>Acciones: (ninguna) | login | logout | marca | registrarMarca.</p>
 */
public class RboServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(RboServlet.class.getName());

    /** Atributo de sesión con el {@link Empleado} autenticado. */
    public static final String SESION_EMPLEADO = "empleado";
    /** Atributo de sesión con el {@link EstadoMarca} actual. */
    public static final String SESION_ESTADO = "estadoMarca";
    /** Atributo de sesión que marca una sesión de prueba creada en modo desarrollo. */
    public static final String SESION_DE_PRUEBA = "sesionDePrueba";
    /**
     * Atributo de request con la interfaz que se está viendo ("admin", "rh", "marca").
     * Lo lee TopMenu.jsp para resaltar la opción activa del selector de interfaces.
     */
    public static final String ATRIBUTO_INTERFAZ = "interfaz";

    /** Las vistas viven en WEB-INF para que no se puedan pedir sin pasar por el controlador. */
    static final String VISTAS = "/WEB-INF/jsp/";

    private DataManager dataManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOG.info("*** Inicializando el controlador RboServlet.");
        dataManager = DataManager.desde(config.getServletContext());
        try {
            dataManager.asegurarEsquema();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo verificar el esquema al arrancar (se reintentará en el login): {0}",
                    e.getMessage());
        }
    }

    /** Al detener o redesplegar la aplicación, libera el driver JDBC. */
    @Override
    public void destroy() {
        LOG.info("*** Deteniendo el controlador RboServlet.");
        dataManager.cerrar();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        boolean esPost = "POST".equalsIgnoreCase(request.getMethod());

        if (action == null) {
            action = "";
        }
        switch (action) {
            case "login":
                if (esPost) {
                    procesarLogin(request, response);
                } else {
                    mostrarLogin(request, response);
                }
                break;
            case "logout":
                cerrarSesion(request, response);
                break;
            case "marca":
                mostrarMarca(request, response);
                break;
            case "registrarMarca":
                if (esPost) {
                    registrarMarca(request, response);
                } else {
                    mostrarMarca(request, response);
                }
                break;
            default:
                mostrarLogin(request, response);
                break;
        }
    }

    // ------------------------------------------------------------------ login / logout

    private void mostrarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Empleado empleado = empleadoEnSesion(request);
        if (empleado != null) {
            response.sendRedirect(urlDeInicio(request, empleado));
            return;
        }
        forward(request, response, "Login.jsp");
    }

    private void procesarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String carnet = request.getParameter("carnet");
        String password = request.getParameter("password");

        if (carnet == null || carnet.isBlank() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Debe introducir su carnet y contraseña.");
            forward(request, response, "Login.jsp");
            return;
        }

        try {
            Empleado empleado = dataManager.validarCredenciales(carnet.trim(), password);
            if (empleado == null) {
                request.setAttribute("error", "Carnet o contraseña incorrectos.");
                forward(request, response, "Login.jsp");
                return;
            }
            HttpSession session = request.getSession(true);
            request.changeSessionId(); // evita la fijación de sesión al autenticar
            session.removeAttribute(SESION_DE_PRUEBA);
            session.setAttribute(SESION_EMPLEADO, empleado);
            session.setAttribute(SESION_ESTADO, EstadoMarca.SIN_JORNADA);
            response.sendRedirect(urlDeInicio(request, empleado));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos en el login", e);
            request.setAttribute("error", "Error de conexión con la base de datos: " + e.getMessage());
            forward(request, response, "Login.jsp");
        } catch (IllegalStateException e) {
            LOG.log(Level.SEVERE, "Configuración incompleta en el login", e);
            request.setAttribute("error", e.getMessage());
            forward(request, response, "Login.jsp");
        }
    }

    private void cerrarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/rbo");
    }

    /**
     * Página de inicio según el rol. El admin va a la Interfaz de Admin y RH a la Interfaz
     * de RH. Contabilidad todavía no tiene interfaz propia, así que va a la marca de asistencia.
     */
    static String urlDeInicio(HttpServletRequest request, Empleado empleado) {
        String contexto = request.getContextPath();
        if (empleado.isAdmin()) {
            return contexto + "/admin";
        }
        if (empleado.isRH()) {
            return contexto + "/rh";
        }
        return contexto + "/rbo?action=marca";
    }

    // ------------------------------------------------------------------ marca

    private void mostrarMarca(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (empleadoEnSesion(request) == null) {
            response.sendRedirect(request.getContextPath() + "/rbo");
            return;
        }
        request.setAttribute("estado", estadoEnSesion(request.getSession(false)));
        forward(request, response, "MarcaEmpleado.jsp");
    }

    private void registrarMarca(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Empleado empleado = empleadoEnSesion(request);
        if (empleado == null) {
            response.sendRedirect(request.getContextPath() + "/rbo");
            return;
        }
        HttpSession session = request.getSession(false);
        AccionMarca accion = AccionMarca.desdeParametro(request.getParameter("tipo_marca"));
        if (accion != null) {
            Marca marca = new Marca(empleado.getIdEmpleado(), accion.getTipoMarcaDB(),
                    accion.comentarioPara(request.getParameter("comentario")));
            try {
                int idGenerado = dataManager.registrarMarca(marca);
                // El estado solo avanza si la marca se guardó en la base.
                session.setAttribute(SESION_ESTADO, EstadoMarca.despuesDe(accion));
                Marca marcaRegistrada = MarcaPeer.getMarcaPorId(dataManager, idGenerado);
                dataManager.logMarca(empleado.getIdEmpleado(), marcaRegistrada);
                request.setAttribute("mensaje", accion.getMensajeConfirmacion());
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "No se pudo registrar la marca", e);
                request.setAttribute("mensaje", "Error al registrar la marca: " + e.getMessage());
            }
        }
        request.setAttribute("estado", estadoEnSesion(session));
        forward(request, response, "MarcaEmpleado.jsp");
    }

    // ------------------------------------------------------------------ utilidades

    private static Empleado empleadoEnSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESION_DE_PRUEBA) != null) {
            return null; // una sesión de prueba del modo desarrollo no sirve fuera de /admin y /rh
        }
        return (Empleado) session.getAttribute(SESION_EMPLEADO);
    }

    private static EstadoMarca estadoEnSesion(HttpSession session) {
        Object estado = (session == null) ? null : session.getAttribute(SESION_ESTADO);
        return (estado instanceof EstadoMarca) ? (EstadoMarca) estado : EstadoMarca.SIN_JORNADA;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.setAttribute(ATRIBUTO_INTERFAZ, "marca");
        getServletContext().getRequestDispatcher(VISTAS + vista).forward(request, response);
    }
}
