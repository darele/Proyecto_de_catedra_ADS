package sv.edu.udb.rbo.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import sv.edu.udb.rbo.beans.Tarifa;

/**
 * SQL de la tabla Tarifa (clase "Peer" del libro, Capítulo 5).
 *
 * <p>Solo la llama el DataManager. Reemplaza a los antiguos TarifaDAO, HistorialTarifasDAO y
 * RHEstadisticasDAO del paquete recursoshumanos.</p>
 */
public final class TarifaPeer {

    /** Cuántas tarifas muestra el historial de la Interfaz de RH. */
    public static final int LIMITE_HISTORIAL = 100;

    private TarifaPeer() {
    }

    public static int insertar(DataManager dataManager, Tarifa tarifa) throws SQLException {
        String sql = "INSERT INTO Tarifa (fecha_tarifa, hora_entrada, hora_salida, tarifa, id_empleado)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, tarifa.getFechaTarifa());
            ps.setTime(2, tarifa.getHoraEntrada());
            ps.setTime(3, tarifa.getHoraSalida());
            ps.setDouble(4, tarifa.getTarifa());
            ps.setString(5, tarifa.getIdEmpleado());
            return ps.executeUpdate();
        }
    }

    /** Las {@value #LIMITE_HISTORIAL} tarifas más recientes, con el nombre del empleado. */
    public static List<Tarifa> listarRecientes(DataManager dataManager) throws SQLException {
        String sql = "SELECT t.id_tarifa, t.id_empleado, t.fecha_tarifa, t.hora_entrada, t.hora_salida, t.tarifa,"
                + " e.nombre, e.Apellido"
                + " FROM Tarifa t LEFT JOIN Empleado e ON e.id_empleado = t.id_empleado"
                + " ORDER BY t.id_tarifa DESC LIMIT " + LIMITE_HISTORIAL;
        List<Tarifa> tarifas = new ArrayList<>();
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Tarifa tarifa = new Tarifa(
                        rs.getString("id_empleado"),
                        rs.getDate("fecha_tarifa"),
                        rs.getTime("hora_entrada"),
                        rs.getTime("hora_salida"),
                        rs.getDouble("tarifa"));
                tarifa.setIdTarifa(rs.getInt("id_tarifa"));
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("Apellido");
                tarifa.setNombreEmpleado(((nombre == null ? "" : nombre) + " "
                        + (apellido == null ? "" : apellido)).trim());
                tarifas.add(tarifa);
            }
        }
        return tarifas;
    }

    public static int contar(DataManager dataManager) throws SQLException {
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM Tarifa")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
