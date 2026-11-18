
package recursoshumanos;

import basededatos.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistorialTarifasDAO {

    public List<String[]> obtenerHistorial() {

        List<String[]> historial = new ArrayList<>();

        String sql = "SELECT id_tarifa, id_empleado, "
                + "fecha_tarifa, hora_entrada, "
                + "hora_salida, tarifa "
                + "FROM tarifa "
                + "ORDER BY id_tarifa DESC "
                + "LIMIT 100";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String[] registro = {
                    String.valueOf(rs.getInt("id_tarifa")),
                    rs.getString("id_empleado"),
                    rs.getString("fecha_tarifa"),
                    rs.getString("hora_entrada"),
                    rs.getString("hora_salida"),
                    String.format(
                        java.util.Locale.US,
                        "%.2f",
                        rs.getDouble("tarifa")
                    )
                };

                historial.add(registro);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(
                "Error al consultar historial: "
                + e.getMessage()
            );
            throw new IllegalStateException(
                "No se pudo consultar el historial de tarifas", e
            );
        }

        return historial;
    }
}
