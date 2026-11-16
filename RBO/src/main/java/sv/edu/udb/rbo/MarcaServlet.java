package sv.edu.udb.rbo;

import basededatos.Conexion;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "MarcaServlet", urlPatterns = {"/MarcaServlet"})
public class MarcaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("id_empleado") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String idEmpleado = (String) session.getAttribute("id_empleado");
        String tipoMarca = request.getParameter("tipo_marca");
        String comentario = request.getParameter("comentario");

        if (tipoMarca == null || tipoMarca.trim().isEmpty()) {
            response.sendRedirect("marcaEmpleado.jsp");
            return;
        }

        if (comentario == null) {
            comentario = "";
        } else {
            comentario = comentario.trim();
        }

        String tipoMarcaDB;
        String mensajeConfirmacion;

        // Manejo de los estados: Entrada (verde) -> Receso (amarillo) -> Retorno de Receso -> Verde
        if ("Entrada".equalsIgnoreCase(tipoMarca)) {
            tipoMarcaDB = "entrada";
            session.setAttribute("estado_marca", "RECESO"); // Pasa el botón a amarillo ("Receso")
            mensajeConfirmacion = "Marca de entrada registrada correctamente.";

        } else if ("Receso".equalsIgnoreCase(tipoMarca)) {
            tipoMarcaDB = "receso";
            if (comentario.isEmpty()) {
                comentario = "Inicio de receso";
            }
            session.setAttribute("estado_marca", "RETORNO"); // Mantiene amarillo con opción "Regresar de Receso"
            mensajeConfirmacion = "Entrada a receso registrada correctamente.";

        } else if ("Retorno".equalsIgnoreCase(tipoMarca)) {
            // Registra el retorno del receso compatible con ENUM('entrada', 'receso', 'salida')
            tipoMarcaDB = "entrada";
            if (comentario.isEmpty()) {
                comentario = "Retorno de receso";
            } else {
                comentario = "Retorno de receso: " + comentario;
            }
            session.setAttribute("estado_marca", "ENTRADA"); // Vuelve el botón a VERDE nuevamente
            mensajeConfirmacion = "Retorno de receso registrado correctamente.";

        } else {
            tipoMarcaDB = "salida";
            session.setAttribute("estado_marca", "ENTRADA"); // Reinicia a verde al marcar salida
            mensajeConfirmacion = "Marca de salida registrada correctamente.";
        }

        // Asegurar el límite de VARCHAR(100) de la tabla Marca
        if (comentario.length() > 100) {
            comentario = comentario.substring(0, 100);
        }

        String sql = "INSERT INTO Marca (tipo_marca, comentario, id_empleado) VALUES (?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoMarcaDB);
            ps.setString(2, comentario);
            ps.setString(3, idEmpleado);
            ps.executeUpdate();

            request.setAttribute("mensaje", mensajeConfirmacion);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("mensaje", "Error al registrar la marca: " + e.getMessage());
        }

        request.getRequestDispatcher("marcaEmpleado.jsp").forward(request, response);
    }
}