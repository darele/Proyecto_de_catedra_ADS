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
import sv.edu.udb.rbo.model.CampoEmpleado;
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
 * <p>Acciones: (ninguna) o inicio | empleados | nuevo-empleado | eliminar-empleado |
 * editar-empleado (vistas); guardar-empleado | borrar-empleado | actualizar-empleado |
 * reiniciar-password (POST que modifican datos).
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
    static final String VISTA_EDITAR_EMPLEADO = "AdminEditarEmpleado.jsp";

    /** Valor del parámetro "campo" que abre todos los campos a la vez. */
    static final String CAMPO_TODOS = "todos";
    /** Valor de ?ok= tras reiniciar la contraseña. */
    static final String OK_PASSWORD = "password";

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
        // TopMenu.jsp resalta la interfaz activa en el selector de interfaces.
        request.setAttribute(RboServlet.ATRIBUTO_INTERFAZ, "admin");

        if ("guardar-empleado".equals(request.getParameter("action"))) {
            guardarEmpleado(request, response);
            return;
        }
        if ("borrar-empleado".equals(request.getParameter("action"))) {
            borrarEmpleado(request, response, empleado);
            return;
        }
        if ("actualizar-empleado".equals(request.getParameter("action"))) {
            actualizarEmpleado(request, response, empleado);
            return;
        }
        if ("reiniciar-password".equals(request.getParameter("action"))) {
            reiniciarPassword(request, response);
            return;
        }

        String vista = resolverVista(request.getParameter("action"));
        prepararDatos(vista, request);
        getServletContext().getRequestDispatcher(VISTAS + vista).forward(request, response);
    }

    private void guardarEmpleado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Empleado signedEmpleado = (session == null) ? null : (Empleado) session.getAttribute(RboServlet.SESION_EMPLEADO);

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

            dataManager.logNuevoEmpleado(signedEmpleado.getIdEmpleado(), idEmpleado);
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

        HttpSession session = request.getSession(false);
        Empleado signedEmpleado = (session == null) ? null : (Empleado) session.getAttribute(RboServlet.SESION_EMPLEADO);

        String error = null;
        String detalle = null;
        if (idEmpleado == null || idEmpleado.trim().isEmpty()) {
            error = "No se indicó qué empleado eliminar.";
        } else if (idEmpleado.equals(enSesion.getIdEmpleado())) {
            error = "No puede eliminar su propio usuario mientras tiene la sesión abierta.";
        } else {
            try {
                dataManager.logEmpleadoEliminado(signedEmpleado.getIdEmpleado(), idEmpleado);
                dataManager.eliminarEmpleado(idEmpleado);
                response.sendRedirect(request.getContextPath() + "/admin?action=eliminar-empleado&eliminado="
                        + URLEncoder.encode(idEmpleado, "UTF-8"));
                return;
            } catch (BaseDatosException e) {
                error = e.getMessage();
                detalle = e.getDetalleTecnico();
                request.setAttribute("tipoError", e.getTipo().name());
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error al borrar empleado en BD: {0}", e.getMessage());
                request.setAttribute("error", "Error al borrar empleado: " + e.getMessage());
                request.setAttribute("seccion", "empleados");
                getServletContext().getRequestDispatcher(VISTAS + VISTA_NUEVO_EMPLEADO).forward(request, response);
            }
        }

        request.setAttribute("error", error);
        request.setAttribute("errorDetalle", detalle);
        prepararDatos(VISTA_ELIMINAR_EMPLEADO, request);
        getServletContext().getRequestDispatcher(VISTAS + VISTA_ELIMINAR_EMPLEADO).forward(request, response);
    }

    /**
     * Guarda la edición de un empleado. El parámetro "campo" decide el modo:
     * el nombre de un {@link CampoEmpleado} edita solo esa columna; "todos" edita todas.
     * Éxito: redirige al editor con ?ok=campo. Error: vuelve al editor con el campo aún abierto.
     */
    private void actualizarEmpleado(HttpServletRequest request, HttpServletResponse response, Empleado enSesion)
            throws ServletException, IOException {
        if (!"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Editar requiere POST.");
            return;
        }

        HttpSession session = request.getSession(false);
        Empleado signedEmpleado = (session == null) ? null : (Empleado) session.getAttribute(RboServlet.SESION_EMPLEADO);


        String idEmpleado = request.getParameter("idEmpleado");
        String campoParam = request.getParameter("campo");
        boolean esPropio = idEmpleado != null && idEmpleado.equals(enSesion.getIdEmpleado());

        try {
            dataManager.logEmpleadoModificado(signedEmpleado.getIdEmpleado(), idEmpleado);
            if (CAMPO_TODOS.equals(campoParam)) {
                Empleado editado = new Empleado();
                editado.setIdEmpleado(idEmpleado);
                for (CampoEmpleado campo : CampoEmpleado.values()) {
                    campo.asignar(editado, campo.convertir(request.getParameter(campo.getParametro())));
                }
                validarRolPropio(esPropio, editado.getRol());
                dataManager.actualizarEmpleado(editado);
            } else {
                CampoEmpleado campo = CampoEmpleado.desdeParametro(campoParam);
                if (campo == null) {
                    throw new IllegalArgumentException("No se indicó qué campo editar.");
                }
                Object valor = campo.convertir(request.getParameter(campo.getParametro()));
                if (campo == CampoEmpleado.ROL) {
                    validarRolPropio(esPropio, (String) valor);
                }
                dataManager.actualizarCampoEmpleado(idEmpleado, campo, valor);
            }
            if (esPropio) {
                refrescarSesion(request, idEmpleado);
            }
            // El #f-campo devuelve la página a la fila que se editó (no al inicio).
            response.sendRedirect(urlEditor(request, idEmpleado) + "&ok=" + URLEncoder.encode(campoParam, "UTF-8")
                    + "#f-" + URLEncoder.encode(campoParam, "UTF-8"));
        } catch (IllegalArgumentException e) {
            mostrarEditorConError(request, response, e.getMessage(), null, null);
        } catch (BaseDatosException e) {
            mostrarEditorConError(request, response, e.getMessage(), e.getDetalleTecnico(), e.getTipo().name());
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error al modificar empleado en BD: {0}", e.getMessage());
            request.setAttribute("error", "Error al modificar empleado: " + e.getMessage());
            request.setAttribute("seccion", "empleados");
            getServletContext().getRequestDispatcher(VISTAS + VISTA_NUEVO_EMPLEADO).forward(request, response);
        }
    }

    /** Reinicia la contraseña a {@link DataManager#PASSWORD_POR_DEFECTO}. */
    private void reiniciarPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Reiniciar contraseña requiere POST.");
            return;
        }
        String idEmpleado = request.getParameter("idEmpleado");
        try {
            dataManager.reiniciarPassword(idEmpleado);
            response.sendRedirect(urlEditor(request, idEmpleado) + "&ok=" + OK_PASSWORD + "#f-" + OK_PASSWORD);
        } catch (BaseDatosException e) {
            mostrarEditorConError(request, response, e.getMessage(), e.getDetalleTecnico(), e.getTipo().name());
        }
    }

    /** Un admin no puede quitarse a sí mismo el rol admin (perdería el acceso a esta interfaz). */
    private static void validarRolPropio(boolean esPropio, String rolNuevo) {
        if (esPropio && !Empleado.ROL_ADMIN.equals(rolNuevo)) {
            throw new IllegalArgumentException("No puede quitarse a sí mismo el rol de administrador.");
        }
    }

    /** Si el admin se editó a sí mismo, actualiza el bean de la sesión (nombre en la barra superior). */
    private void refrescarSesion(HttpServletRequest request, String idEmpleado) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        try {
            session.setAttribute(RboServlet.SESION_EMPLEADO, dataManager.getEmpleado(idEmpleado));
        } catch (BaseDatosException e) {
            LOG.log(Level.WARNING, "No se pudo refrescar el empleado en sesión: {0}", e.getMessage());
        }
    }

    private void mostrarEditorConError(HttpServletRequest request, HttpServletResponse response,
                                       String error, String detalle, String tipo)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("errorDetalle", detalle);
        request.setAttribute("tipoError", tipo);
        prepararDatos(VISTA_EDITAR_EMPLEADO, request);
        getServletContext().getRequestDispatcher(VISTAS + VISTA_EDITAR_EMPLEADO).forward(request, response);
    }

    private static String urlEditor(HttpServletRequest request, String idEmpleado) throws IOException {
        return request.getContextPath() + "/admin?action=editar-empleado&id="
                + URLEncoder.encode(idEmpleado == null ? "" : idEmpleado, "UTF-8");
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
            case "editar-empleado":
                return VISTA_EDITAR_EMPLEADO;
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
            case VISTA_EDITAR_EMPLEADO:
                prepararEditor(request);
                break;
            case VISTA_INICIO:
            default:
                request.setAttribute("seccion", "inicio");
                request.setAttribute("resumen", dataManager.getResumenAdmin());
                break;
        }
    }

    /**
     * Datos del editor:
     *   emp     el empleado a editar (falta si no se encontró: la vista muestra solo el error);
     *   campo   campo abierto ("nombre", ..., "todos") o ausente si todos están bloqueados;
     *   roles   valores del select de rol;
     *   mensajeOk texto de confirmación tras un redirect con ?ok=.
     * El id llega como "id" (al abrir) o "idEmpleado" (al volver de un POST con error).
     */
    private void prepararEditor(HttpServletRequest request) {
        request.setAttribute("seccion", "empleados");
        request.setAttribute("roles", CampoEmpleado.ROLES);

        String id = request.getParameter("id");
        if (id == null) {
            id = request.getParameter("idEmpleado");
        }
        if (id == null || id.trim().isEmpty()) {
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "No se indicó qué empleado editar.");
            }
            return;
        }
        try {
            request.setAttribute("emp", dataManager.getEmpleado(id));
        } catch (BaseDatosException e) {
            // Si ya hay un error del POST, se conserva; si no, se muestra el de la lectura.
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("errorDetalle", e.getDetalleTecnico());
                request.setAttribute("tipoError", e.getTipo().name());
            }
            return;
        }

        String campo = request.getParameter("campo");
        if (CAMPO_TODOS.equals(campo) || CampoEmpleado.desdeParametro(campo) != null) {
            request.setAttribute("campo", campo);
        }

        String ok = request.getParameter("ok");
        if (ok != null && request.getAttribute("error") == null) {
            CampoEmpleado campoOk = CampoEmpleado.desdeParametro(ok);
            if (OK_PASSWORD.equals(ok)) {
                request.setAttribute("mensajeOk",
                        "Se reinició la contraseña a \"" + DataManager.PASSWORD_POR_DEFECTO + "\".");
            } else if (CAMPO_TODOS.equals(ok)) {
                request.setAttribute("mensajeOk", "Se guardaron todos los cambios.");
            } else if (campoOk != null) {
                request.setAttribute("mensajeOk", "Se actualizó: " + campoOk.getEtiqueta() + ".");
            }
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
