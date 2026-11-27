package sv.edu.udb.rbo.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sv.edu.udb.rbo.beans.Marca;

/**
 * SQL de la tabla Marca (clase "Peer" del libro, Capítulo 5).
 */
public final class MarcaPeer {

    private MarcaPeer() {
    }

    /** La fecha y la hora las pone la base con DEFAULT (CURRENT_DATE) / (CURRENT_TIME). */
    public static int insertarMarca(DataManager dataManager, Marca marca) throws SQLException {
        String sql = "INSERT INTO Marca (tipo_marca, comentario, id_empleado) VALUES (?, ?, ?)";
        try (Connection connection = dataManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, marca.getTipoMarca());
            ps.setString(2, marca.getComentario());
            ps.setString(3, marca.getIdEmpleado());
            int filasAfectadas = ps.executeUpdate();
            int idMarca = -1;
            if (filasAfectadas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        idMarca = keys.getInt(1);
                    }
                }
            }
            return idMarca;
        }
    }

    public static int contarDeHoy(DataManager dataManager) throws SQLException {
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM Marca WHERE fecha_marca = CURRENT_DATE")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static Marca getMarcaPorId(DataManager dataManager, int idMarca) throws SQLException {
        String sql = "SELECT id_marca, fecha_marca, hora_marca, tipo_marca, comentario, id_empleado"
                + " FROM marca WHERE id_marca = ?";
        try (Connection connection = dataManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(idMarca));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Marca ans = new Marca(
                        rs.getString("id_empleado"),
                        rs.getString("tipo_marca"),
                        rs.getString("comentario")
                );
                ans.setIdMarca(rs.getInt("id_marca"));
                ans.setHoraMarca(rs.getTime("hora_marca"));
                ans.setFechaMarca(rs.getTime("fecha_marca"));
                return ans;
            }
        }
    }
}
