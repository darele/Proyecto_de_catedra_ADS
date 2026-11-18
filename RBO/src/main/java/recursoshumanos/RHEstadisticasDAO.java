
package recursoshumanos;

import basededatos.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RHEstadisticasDAO {

    public int contarEmpleados() {
        return contarRegistros("SELECT COUNT(*) FROM empleado");
    }

    public int contarTarifas() {
        return contarRegistros("SELECT COUNT(*) FROM tarifa");
    }

    private int contarRegistros(String sql) {

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(
                "Error al consultar estadísticas: "
                + e.getMessage()
            );
        }

        return -1;
    }
}
