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
    public static void insertarMarca(DataManager dataManager, Marca marca) throws SQLException {
        String sql = "INSERT INTO Marca (tipo_marca, comentario, id_empleado) VALUES (?, ?, ?)";
        try (Connection connection = dataManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, marca.getTipoMarca());
            ps.setString(2, marca.getComentario());
            ps.setString(3, marca.getIdEmpleado());
            ps.executeUpdate();
        }
    }

    public static int contarDeHoy(DataManager dataManager) throws SQLException {
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM Marca WHERE fecha_marca = CURRENT_DATE")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
