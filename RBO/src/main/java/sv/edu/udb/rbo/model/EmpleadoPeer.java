package sv.edu.udb.rbo.model;

import java.sql.*;
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
        String sql = "INSERT INTO empleado (id_empleado, nombre, Apellido, tarifa, hora_entrada, hora_salida, tiempo_receso, rol, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = dataManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setString(1, empleado.getIdEmpleado());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getApellido());

            if(empleado.getTarifa() != null){
                ps.setDouble(4, empleado.getTarifa());
            }else {
                ps.setNull(4, Types.DOUBLE);
            }

            ps.setTime(5, empleado.getHoraEntrada());
            ps.setTime(6, empleado.getHoraSalida());
            ps.setInt(7, empleado.getTiempoReceso());
            ps.setString(8, empleado.getRol());
            ps.setString(9, passwordHash);

            return ps.executeUpdate();
        }
    }

    /**
     * Borra un empleado por su carnet.
     *
     * <p>Si el empleado tiene filas en Marca o Tarifa, MariaDB rechaza el DELETE con el
     * código 1451 (llave foránea). La SQLException se deja subir tal cual: el DataManager
     * la clasifica con {@link BaseDatosException#desde}.</p>
     *
     * @return filas eliminadas (0 si el carnet no existe, 1 si se borró).
     */
    public static int eliminar(DataManager dataManager, String idEmpleado) throws SQLException {
        String sql = "DELETE FROM Empleado WHERE id_empleado = ?";
        try (Connection connection = dataManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, idEmpleado);
            return ps.executeUpdate();
        }
    }
}
