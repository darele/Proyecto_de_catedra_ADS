package sv.edu.udb.rbo;

import basededatos.Conexion;
import basededatos.DBHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String carnet = request.getParameter("carnet");
        String passwordPlano = request.getParameter("password");

        if (carnet == null || carnet.trim().isEmpty() || passwordPlano == null || passwordPlano.isEmpty()) {
            request.setAttribute("error", "Debe introducir su carnet y contraseña.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        carnet = carnet.trim();
        String passwordHash = SeguridadUtil.encriptarSHA256(passwordPlano);

        try {
            // Asegura que las tablas Empleado, Marca y Tarifa estén creadas
            new DBHandler().init();

            String sql = "SELECT id_empleado, nombre, Apellido, rol FROM Empleado WHERE id_empleado = ? AND password = ?";
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, carnet);
                ps.setString(2, passwordHash);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String nombreCompleto = rs.getString("nombre") + " " + rs.getString("Apellido");
                        String rol = rs.getString("rol");

                        HttpSession session = request.getSession();
                        session.setAttribute("id_empleado", rs.getString("id_empleado"));
                        session.setAttribute("nombre_empleado", nombreCompleto);
                        session.setAttribute("rol", rol);

                        // Redirección según los valores del ENUM('admin', 'RH', 'Contabilidad', 'Empleado')
                        if ("admin".equalsIgnoreCase(rol)) {
                            response.sendRedirect("admin.jsp");
                        } else if ("RH".equalsIgnoreCase(rol)) {
                            response.sendRedirect("rh.jsp");
                        } else if ("Contabilidad".equalsIgnoreCase(rol)) {
                            response.sendRedirect("contabilidad.jsp");
                        } else {
                            response.sendRedirect("marcaEmpleado.jsp");
                        }
                    } else {
                        request.setAttribute("error", "Carnet o contraseña incorrectos.");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error de conexión con la base de datos: " + e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}