package sv.edu.udb.rbo.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import sv.edu.udb.rbo.beans.Empleado;

/**
 * SQL de la tabla Empleado (clase "Peer" del libro, Capítulo 5).
 *
 * <p>Métodos estáticos que reciben el DataManager como primer argumento. Solo los llama
 * el DataManager. Usan PreparedStatement en lugar de concatenar texto como en el libro,
 * para evitar inyección SQL.</p>
 */
public final class EmpleadoPeer {

    private EmpleadoPeer() {
    }

    public static Empleado buscarPorCredenciales(DataManager dataManager, String carnet, String passwordHash)
            throws SQLException {
        String sql = "SELECT id_empleado, nombre, Apellido, rol FROM Empleado"
                + " WHERE id_empleado = ? AND password = ?";
        try (Connection connection = dataManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, carnet);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerEmpleado(rs) : null;
            }
        }
    }

    public static List<Empleado> listarTodos(DataManager dataManager) throws SQLException {
        String sql = "SELECT id_empleado, nombre, Apellido, rol FROM Empleado ORDER BY Apellido, nombre";
        List<Empleado> empleados = new ArrayList<>();
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                empleados.add(leerEmpleado(rs));
            }
        }
        return empleados;
    }

    public static int contar(DataManager dataManager) throws SQLException {
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM Empleado")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static Empleado leerEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getString("id_empleado"),
                rs.getString("nombre"),
                rs.getString("Apellido"),
                rs.getString("rol"));
    }

    public static int insertar(DataManager dataManager, Empleado empleado, String passwordHash) throws SQLException{
        return 0;
    }
}
