package sv.edu.udb.rbo.rh;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
import sv.edu.udb.rbo.beans.Tarifa;
import sv.edu.udb.rbo.model.BaseDatosException;
import sv.edu.udb.rbo.model.DataManager;

/**
 * Controlador de la Interfaz de RH (Recursos Humanos).
 *
 * <p>Sigue el mismo patrón que {@link sv.edu.udb.rbo.admin.AdminServlet}: cada rol tiene su
 * controlador, su paquete y sus vistas (WEB-INF/jsp/rh/), y comparte con las demás interfaces
 * el modelo (DataManager) y la sesión del login. Reemplaza al antiguo paquete
 * {@code recursoshumanos} (TarifaServlet + DAOs + RH/RHindex.jsp).</p>
 *
 * <p>Se llega aquí después del login cuando el rol es "RH" (RboServlet redirige a {@code /rh}).
 * El admin también puede entrar. Con el modo desarrollo activo (ver {@link ModoDesarrollo}),
 * si nadie inició sesión se crea una sesión de prueba con un usuario de RH ficticio.</p>
 *
 * <p>Acciones: (ninguna) o inicio | empleados | tarifas (vistas);
 * registrar-tarifa (POST que guarda una tarifa).
 * Para agregar una pantalla: (1) un case en {@link #resolverVista(String)}, (2) cargar sus
 * datos en {@link #prepararDatos}, (3) crear su JSP en WEB-INF/jsp/rh/ y (4) un enlace en
 * RHMenu.jsp.</p>
 */
public class RHServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(RHServlet.class.getName());

    static final String VISTAS = "/WEB-INF/jsp/rh/";
    static final String VISTA_INICIO = "RHInicio.jsp";
    static final String VISTA_EMPLEADOS = "RHEmpleados.jsp";
    static final String VISTA_TARIFAS = "RHTarifas.jsp";

    /** Largo máximo del carnet (columna id_empleado VARCHAR(10)). */
    static final int LARGO_CARNET = 10;

    private DataManager dataManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOG.info("*** Inicializando el controlador RHServlet.");
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
                    "La Interfaz de RH es solo para los roles RH y admin.");
            return;
        }
        // TopMenu.jsp resalta la interfaz activa en el selector de interfaces.
        request.setAttribute(RboServlet.ATRIBUTO_INTERFAZ, "rh");

        if ("registrar-tarifa".equals(request.getParameter("action"))) {
            registrarTarifa(request, response);
            return;
        }

        String vista = resolverVista(request.getParameter("action"));
        prepararDatos(vista, request);
        forward(request, response, vista);
    }

    /**
     * Guarda una tarifa. Solo por POST.
     * Éxito: redirige al historial con ?ok=1 (patrón PRG, recargar no la repite).
     * Error: vuelve al formulario con el mensaje y los valores que se escribieron.
     */
    private void registrarTarifa(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Registrar una tarifa requiere POST.");
            return;
        }
        try {
            Tarifa tarifa = validarTarifa(
                    request.getParameter("idEmpleado"),
                    request.getParameter("fecha"),
                    request.getParameter("horaEntrada"),
                    request.getParameter("horaSalida"),
                    request.getParameter("tarifa"));
            dataManager.registrarTarifa(tarifa);
            response.sendRedirect(request.getContextPath() + "/rh?action=tarifas&ok=1");
        } catch (IllegalArgumentException e) {
            mostrarTarifasConError(request, response, e.getMessage(), null);
        } catch (BaseDatosException e) {
            mostrarTarifasConError(request, response, e.getMessage(), e.getDetalleTecnico());
        }
    }

    private void mostrarTarifasConError(HttpServletRequest request, HttpServletResponse response,
                                        String error, String detalle)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("errorDetalle", detalle);
        prepararDatos(VISTA_TARIFAS, request);
        forward(request, response, VISTA_TARIFAS);
    }

    /**
     * Valida los datos del formulario y arma el bean. Es estática y pura para probarla con JUnit.
     *
     * @throws IllegalArgumentException con un mensaje listo para mostrar si algún dato no es válido.
     */
    static Tarifa validarTarifa(String idEmpleado, String fecha, String entrada, String salida, String monto) {
        if (vacio(idEmpleado)) {
            throw new IllegalArgumentException("Seleccione el empleado.");
        }
        idEmpleado = idEmpleado.trim();
        if (idEmpleado.length() > LARGO_CARNET) {
            throw new IllegalArgumentException("El carnet no puede superar " + LARGO_CARNET + " caracteres.");
        }

        if (vacio(monto)) {
            throw new IllegalArgumentException("Ingrese una tarifa.");
        }
        double valor;
        try {
            valor = Double.parseDouble(monto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingrese una tarifa numérica válida.");
        }
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser mayor que cero.");
        }

        if (vacio(fecha)) {
            throw new IllegalArgumentException("Seleccione una fecha.");
        }
        if (vacio(entrada) || vacio(salida)) {
            throw new IllegalArgumentException("Complete los horarios.");
        }
        LocalDate fechaTarifa;
        LocalTime horaEntrada;
        LocalTime horaSalida;
        try {
            fechaTarifa = LocalDate.parse(fecha.trim());
            horaEntrada = LocalTime.parse(entrada.trim());
            horaSalida = LocalTime.parse(salida.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha o los horarios no son válidos.");
        }
        if (!horaSalida.isAfter(horaEntrada)) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de entrada.");
        }

        return new Tarifa(idEmpleado, Date.valueOf(fechaTarifa), Time.valueOf(horaEntrada),
                Time.valueOf(horaSalida), valor);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    /** Regla de acceso: roles RH y admin. Es estática y pura para probarla con JUnit. */
    static boolean puedeEntrar(Empleado empleado) {
        return empleado != null && (empleado.isRH() || empleado.isAdmin());
    }

    /** Elige la vista según el parámetro action. Un action desconocido muestra la portada. */
    static String resolverVista(String action) {
        if (action == null) {
            return VISTA_INICIO;
        }
        switch (action) {
            case "empleados":
                return VISTA_EMPLEADOS;
            case "tarifas":
                return VISTA_TARIFAS;
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
                cargarEmpleados(request);
                break;
            case VISTA_TARIFAS:
                request.setAttribute("seccion", "tarifas");
                request.setAttribute("hoy", LocalDate.now().toString());
                cargarEmpleados(request); // para el <select> del formulario
                try {
                    request.setAttribute("tarifas", dataManager.getTarifasRecientes());
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "No se pudo leer el historial de tarifas: {0}", e.getMessage());
                    request.setAttribute("errorHistorial", "No se pudo leer el historial de tarifas: " + e.getMessage());
                }
                if (request.getParameter("ok") != null && request.getAttribute("error") == null) {
                    request.setAttribute("mensajeOk", "Tarifa registrada correctamente.");
                }
                break;
            case VISTA_INICIO:
            default:
                request.setAttribute("seccion", "inicio");
                request.setAttribute("resumen", dataManager.getResumenRH());
                break;
        }
    }

    private void cargarEmpleados(HttpServletRequest request) {
        try {
            request.setAttribute("empleados", dataManager.getEmpleados());
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo listar empleados: {0}", e.getMessage());
            request.setAttribute("errorEmpleados", "No se pudo leer la lista de empleados: " + e.getMessage());
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher(VISTAS + vista).forward(request, response);
    }

    private static Empleado iniciarSesionDePrueba(HttpServletRequest request) {
        Empleado demo = ModoDesarrollo.rhDePrueba();
        HttpSession session = request.getSession(true);
        session.setAttribute(RboServlet.SESION_EMPLEADO, demo);
        session.setAttribute(RboServlet.SESION_DE_PRUEBA, Boolean.TRUE);
        LOG.warning("Modo desarrollo: se creó una sesión de prueba con un usuario de RH ficticio.");
        return demo;
    }
}
