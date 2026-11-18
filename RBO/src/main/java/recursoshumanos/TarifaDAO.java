
package recursoshumanos;

import basededatos.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;

public class TarifaDAO {

    public boolean registrarTarifa(
            String idEmpleado,
            Date fecha,
            Time horaEntrada,
            Time horaSalida,
            double tarifa) {

        String sql = "INSERT INTO tarifa "
                + "(fecha_tarifa, hora_entrada, hora_salida, "
                + "tarifa, id_empleado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, fecha);
            ps.setTime(2, horaEntrada);
            ps.setTime(3, horaSalida);
            ps.setDouble(4, tarifa);
            ps.setString(5, idEmpleado);

            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(
                "Error al registrar tarifa: " + e.getMessage()
            );
            return false;
        }
    }
}
