package sv.edu.udb.rbo.model;

import sv.edu.udb.rbo.beans.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditoriaPeer {

    private static int logCambio(DataManager dataManager,
                                 String accion, String nombreTabla,
                                 String llavePrimaria, String usuario,
                                 String estadoAnterior) throws SQLException {
        String sql = "INSERT INTO auditoria (accion, nombre_tabla, llave_primaria, usuario, estado_anterior) " +
                "VALUES (?, ?, ?, ?, ?)";

        try(Connection connection = dataManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setString(1, accion);
            ps.setString(2, nombreTabla);
            ps.setString(3, llavePrimaria);
            ps.setString(4, usuario);
            ps.setString(5, estadoAnterior);

            return ps.executeUpdate();
        }
    }

    public static int logNuevoEmpleado(DataManager dataManager, String signedUser, String idNuevoEmpleado) throws SQLException {
        return logCambio(dataManager, "insertar", "empleado",
                idNuevoEmpleado, signedUser, "");
    }

    private static String getInfoEmpleadoRaw(DataManager dataManager, String id) throws SQLException {
        Empleado actualEmpleado = EmpleadoPeer.buscarPorId(dataManager, id);
        StringBuilder infoActualRaw = new StringBuilder();
        infoActualRaw.append(actualEmpleado.getNombre()).append(", ");
        infoActualRaw.append(actualEmpleado.getApellido()).append(", ");
        infoActualRaw.append(actualEmpleado.getTarifa() != null ? actualEmpleado.getTarifa() : "").append(", ");
        infoActualRaw.append(actualEmpleado.getHoraEntrada()).append(", ");
        infoActualRaw.append(actualEmpleado.getHoraSalida()).append(", ");
        infoActualRaw.append(actualEmpleado.getTiempoReceso()).append(", ");
        infoActualRaw.append(actualEmpleado.getRol());
        return infoActualRaw.toString();
    }

    public static int logEmpleadoModificado(DataManager dataManager, String signedUser, String idNuevoEmpleado) throws SQLException {
        return logCambio(dataManager, "modificar",
                "empleado",
                idNuevoEmpleado,
                signedUser,
                getInfoEmpleadoRaw(dataManager, idNuevoEmpleado));
    }

    public static int logEmpleadoBorrado(DataManager dataManager, String signedUser, String idEmpleadoEliminado) throws SQLException {
        return logCambio(dataManager, "borrar",
                "empleado",
                idEmpleadoEliminado,
                signedUser,
                getInfoEmpleadoRaw(dataManager, idEmpleadoEliminado));
    }

}
