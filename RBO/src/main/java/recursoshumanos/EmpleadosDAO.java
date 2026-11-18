
package recursoshumanos;

import basededatos.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadosDAO {

    public List<String[]> obtenerEmpleados() {

        List<String[]> empleados = new ArrayList<>();

        String sql = "SELECT id_empleado, nombre, "
                + "Apellido, rol, hora_entrada, hora_salida "
                + "FROM empleado "
                + "ORDER BY nombre ASC, Apellido ASC "
                + "LIMIT 100";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String[] empleado = {
                    rs.getString("id_empleado"),
                    rs.getString("nombre"),
                    rs.getString("Apellido"),
                    rs.getString("rol"),
                    rs.getString("hora_entrada"),
                    rs.getString("hora_salida")
                };

                empleados.add(empleado);
            }

        } catch (SQLException | ClassNotFoundException e) {

            System.err.println(
                "Error al consultar empleados: "
                + e.getMessage()
            );

            throw new IllegalStateException(
                "No se pudo consultar la lista de empleados",
                e
            );
        }

        return empleados;
    }
}
