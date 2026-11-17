package sv.edu.udb.rbo.admin;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.Time;
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
import sv.edu.udb.rbo.model.BaseDatosException;
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
 * <p>Acciones: (ninguna) o inicio | empleados | nuevo-empleado | eliminar-empleado (vistas);
 * guardar-empleado | borrar-empleado (POST que modifican datos).
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
    static final String VISTA_NUEVO_EMPLEADO = "AdminNuevoEmpleado.jsp";
    static final String VISTA_ELIMINAR_EMPLEADO = "AdminEliminarEmpleado.jsp";

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

        if ("guardar-empleado".equals(request.getParameter("action"))) {
            guardarEmpleado(request, response);
            return;
        }
        if ("borrar-empleado".equals(request.getParameter("action"))) {
            borrarEmpleado(request, response, empleado);
            return;
        }

        String vista = resolverVista(request.getParameter("action"));
        prepararDatos(vista, request);
        getServletContext().getRequestDispatcher(VISTAS + vista).forward(request, response);
    }

    private void guardarEmpleado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idEmpleado = request.getParameter("idEmpleado");
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");

        String tarifaStr = request.getParameter("tarifa");
        Double tarifa = (tarifaStr != null && !tarifaStr.trim().isEmpty())
                ? Double.parseDouble(tarifaStr) : null;

        // Los inputs HTML <input type="time"> devuelven "HH:mm". java.sql.Time requiere "HH:mm:ss"
        String horaEntradaStr = request.getParameter("hora_entrada");
        if (horaEntradaStr != null && horaEntradaStr.length() == 5) horaEntradaStr += ":00";
        assert horaEntradaStr != null;
        Time horaEntrada = Time.valueOf(horaEntradaStr);

        String horaSalidaStr = request.getParameter("hora_salida");
        if (horaSalidaStr != null && horaSalidaStr.length() == 5) horaSalidaStr += ":00";
        assert horaSalidaStr != null;
        Time horaSalida = Time.valueOf(horaSalidaStr);

        int tiempoReceso = Integer.parseInt(request.getParameter("tiempo_receso"));
        String rol = request.getParameter("rol");
        String password = request.getParameter("password");

        try {
            Empleado nuevoEmp = new Empleado(idEmpleado, nombre, apellido, tarifa, horaEntrada,
                    horaSalida, tiempoReceso, rol);
            dataManager.registrarEmpleado(nuevoEmp, password);
            response.sendRedirect(request.getContextPath() + "/admin?action=empleados");
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error al registrar empleado en BD: {0}", e.getMessage());
            request.setAttribute("error", "Error al registrar empleado (¿Carnet duplicado?): " + e.getMessage());
            request.setAttribute("seccion", "empleados");
            getServletContext().getRequestDispatcher(VISTAS + VISTA_NUEVO_EMPLEADO).forward(request, response);
        }
    }

    /**
     * Elimina un empleado. Solo por POST (un enlace GET podría dispararse con solo abrir una URL).
     * Éxito: redirige a la lista con ?eliminado=ID (patrón PRG, recargar no repite el borrado).
     * Error: vuelve a mostrar la lista con el mensaje de {@link BaseDatosException}.
     */
    private void borrarEmpleado(HttpServletRequest request, HttpServletResponse response, Empleado enSesion)
            throws ServletException, IOException {
        String idEmpleado = request.getParameter("idEmpleado");

        if (!"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Eliminar requiere POST.");
            return;
        }

        String error = null;
        String detalle = null;
        if (idEmpleado == null || idEmpleado.trim().isEmpty()) {
            error = "No se indicó qué empleado eliminar.";
        } else if (idEmpleado.equals(enSesion.getIdEmpleado())) {
            error = "No puede eliminar su propio usuario mientras tiene la sesión abierta.";
        } else {
            try {
                dataManager.eliminarEmpleado(idEmpleado);
                response.sendRedirect(request.getContextPath() + "/admin?action=eliminar-empleado&eliminado="
                        + URLEncoder.encode(idEmpleado, "UTF-8"));
                return;
            } catch (BaseDatosException e) {
                error = e.getMessage();
                detalle = e.getDetalleTecnico();
                request.setAttribute("tipoError", e.getTipo().name());
            }
        }

        request.setAttribute("error", error);
        request.setAttribute("errorDetalle", detalle);
        prepararDatos(VISTA_ELIMINAR_EMPLEADO, request);
        getServletContext().getRequestDispatcher(VISTAS + VISTA_ELIMINAR_EMPLEADO).forward(request, response);
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
            case "nuevo-empleado":
                return VISTA_NUEVO_EMPLEADO;
            case "eliminar-empleado":
                return VISTA_ELIMINAR_EMPLEADO;
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
            case VISTA_NUEVO_EMPLEADO:
                request.setAttribute("seccion", "empleados");
                break;
            case VISTA_ELIMINAR_EMPLEADO:
                request.setAttribute("seccion", "empleados");
                try {
                    request.setAttribute("empleados", dataManager.getEmpleados());
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "No se pudo listar empleados: {0}", e.getMessage());
                    // No pisar el error de un borrado fallido si ya hay uno.
                    if (request.getAttribute("error") == null) {
                        request.setAttribute("error", "No se pudo leer la lista de empleados: " + e.getMessage());
                    }
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
