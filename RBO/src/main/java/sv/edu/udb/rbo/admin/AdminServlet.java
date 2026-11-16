package sv.edu.udb.rbo.admin;

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
import sv.edu.udb.rbo.ModoDesarrollo;
import sv.edu.udb.rbo.RboServlet;
import sv.edu.udb.rbo.beans.Empleado;
import sv.edu.udb.rbo.model.DataManager;

/**
 * Controlador de la Interfaz de Admin ("interfaz-admin").
 *
 * <p>El libro dice que cada rol tiene su propia interfaz (Capítulo 4: "the two main roles
 * are the administrator and the customer"). Por eso la interfaz de administración tiene
 * su controlador, su paquete y sus vistas (WEB-INF/jsp/admin/) separados de la interfaz
 * de empleado, y comparte con ella el modelo (DataManager) y la sesión del login.</p>
 *
 * <p>Se llega aquí después del login cuando el rol es "admin" (RboServlet redirige a
 * {@code /admin}). Para probarla de forma desacoplada, active el modo desarrollo
 * (ver {@link ModoDesarrollo}): si nadie inició sesión, se crea una sesión de prueba
 * con un admin ficticio.</p>
 *
 * <p>Acciones: (ninguna) o inicio | empleados.
 * Para agregar una pantalla: (1) un case en {@link #resolverVista(String)}, (2) cargar sus
 * datos en {@link #prepararDatos}, (3) crear su JSP en WEB-INF/jsp/admin/ y (4) un enlace en
 * AdminMenu.jsp.</p>
 */
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AdminServlet.class.getName());

    static final String VISTAS = "/WEB-INF/jsp/admin/";
    static final String VISTA_INICIO = "AdminInicio.jsp";
    static final String VISTA_EMPLEADOS = "AdminEmpleados.jsp";

    private DataManager dataManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOG.info("*** Inicializando el controlador AdminServlet.");
        dataManager = DataManager.desde(config.getServletContext());
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

        HttpSession session = request.getSession(false);
        Empleado empleado = (session == null) ? null : (Empleado) session.getAttribute(RboServlet.SESION_EMPLEADO);
        boolean modoDesarrollo = ModoDesarrollo.estaActivo(getServletContext());

        // Una sesión de prueba deja de valer si el modo desarrollo se apaga.
        if (session != null && session.getAttribute(RboServlet.SESION_DE_PRUEBA) != null && !modoDesarrollo) {
            session.invalidate();
            empleado = null;
        }

        if (empleado == null && modoDesarrollo) {
            empleado = iniciarSesionDePrueba(request);
        }
        if (empleado == null) {
            response.sendRedirect(request.getContextPath() + "/rbo");
            return;
        }
        if (!puedeEntrar(empleado)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "La Interfaz de Admin es solo para el rol admin.");
            return;
        }

        String vista = resolverVista(request.getParameter("action"));
        prepararDatos(vista, request);
        getServletContext().getRequestDispatcher(VISTAS + vista).forward(request, response);
    }

    /** Regla de acceso: solo el rol admin. Es estática y pura para probarla con JUnit. */
    static boolean puedeEntrar(Empleado empleado) {
        return empleado != null && empleado.isAdmin();
    }

    /**
     * Elige la vista según el parámetro action (switch del ShopServlet del libro).
     * Un action desconocido muestra la portada.
     */
    static String resolverVista(String action) {
        if (action == null) {
            return VISTA_INICIO;
        }
        switch (action) {
            case "empleados":
                return VISTA_EMPLEADOS;
            case "inicio":
            default:
                return VISTA_INICIO;
        }
    }

    /** Carga en el request los datos que necesita cada vista. */
    private void prepararDatos(String vista, HttpServletRequest request) {
        switch (vista) {
            case VISTA_EMPLEADOS:
                request.setAttribute("seccion", "empleados");
                try {
                    request.setAttribute("empleados", dataManager.getEmpleados());
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "No se pudo listar empleados: {0}", e.getMessage());
                    request.setAttribute("error", "No se pudo leer la lista de empleados: " + e.getMessage());
                }
                break;
            case VISTA_INICIO:
            default:
                request.setAttribute("seccion", "inicio");
                request.setAttribute("resumen", dataManager.getResumenAdmin());
                break;
        }
    }

    private static Empleado iniciarSesionDePrueba(HttpServletRequest request) {
        Empleado demo = ModoDesarrollo.adminDePrueba();
        HttpSession session = request.getSession(true);
        session.setAttribute(RboServlet.SESION_EMPLEADO, demo);
        session.setAttribute(RboServlet.SESION_DE_PRUEBA, Boolean.TRUE);
        LOG.warning("Modo desarrollo: se creó una sesión de prueba con un admin ficticio.");
        return demo;
    }
}
