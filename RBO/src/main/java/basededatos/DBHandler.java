package basededatos;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DBHandler {

    private final Conexion conexion;

    public DBHandler() {
        conexion = new Conexion();
    }
    
    public record ForeignKey(String refTable, String refColumn) {

    }

    public record Column(String name, String type,
            boolean primaryKey, boolean notNull,
            ForeignKey foreignKey) {

        public static Column pk(String name, String type) {
            return new Column(name, type, true, true, null);
        }

        public static Column of(String name, String type) {
            return new Column(name, type, false, false, null);
        }

        public static Column notNull(String name, String type) {
            return new Column(name, type, false, true, null);
        }

        public static Column fk(String name, String type, String refTable, String refColumn) {
            return new Column(name, type, false, true, new ForeignKey(refTable, refColumn));
        }
    }

    public void createTable(String tableName, List<Column> columns)
            throws SQLException {

        List<String> parts = columns.stream().map(c -> {
            StringBuilder sb = new StringBuilder(c.name()).append(' ').append(c.type());
            if (c.primaryKey()) {
                sb.append(" PRIMARY KEY");
                if (c.type().equals("INT")) {
                    sb.append(" AUTO_INCREMENT");
                }
            } else if (c.notNull()) {
                sb.append(" NOT NULL");
            }
            return sb.toString();
        }).collect(Collectors.toList());

        columns.stream()
                .filter(c -> c.foreignKey() != null)
                .forEach(c -> parts.add(String.format(
                "FOREIGN KEY (%s) REFERENCES %s(%s)",
                c.name(),
                c.foreignKey().refTable(),
                c.foreignKey().refColumn())));

        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (\n  %s\n)",
                tableName, String.join(",\n  ", parts));

        System.out.println("Executing:\n" + sql);
        conexion.comando.execute(sql);
    }
    
    public void init() {
        try {
            
            //Tabla Empleado
            createTable("Empleado", List.of(
                    DBHandler.Column.pk("id_empleado", "VARCHAR(10)"),
                    DBHandler.Column.notNull("nombre", "VARCHAR(30)"),
                    DBHandler.Column.notNull("Apellido", "VARCHAR(30)"),
                    DBHandler.Column.of("tarifa", "DOUBLE"),
                    DBHandler.Column.notNull("hora_entrada", "TIME"),
                    DBHandler.Column.notNull("hora_salida", "TIME"),
                    DBHandler.Column.notNull("tiempo_receso", "INT"),
                    DBHandler.Column.notNull("rol",
                            "ENUM('admin', 'RH', 'Contabilidad', 'Empleado')"),
                    DBHandler.Column.notNull("password", "VARCHAR(64)")
                )
            );
            
            //Tabla marca
            createTable("Marca", List.of(
                    DBHandler.Column.pk("id_marca", "INT"),
                    DBHandler.Column.notNull("fecha_marca", "DATE DEFAULT (CURRENT_DATE)"),
                    DBHandler.Column.notNull("hora_marca", "TIME DEFAULT (CURRENT_TIME)"),
                    DBHandler.Column.notNull("tipo_marca", "ENUM('entrada', 'receso', 'salida')"),
                    DBHandler.Column.notNull("comentario", "VARCHAR(100)"),
                    DBHandler.Column.fk("id_empleado", "VARCHAR(10)", "Empleado", "id_empleado")
                )
            );
            
            //Tabla Tarifa
            createTable("Tarifa", List.of(
                    DBHandler.Column.pk("id_tarifa", "INT"),
                    DBHandler.Column.notNull("fecha_tarifa", "DATE"),
                    DBHandler.Column.notNull("hora_entrada", "TIME"),
                    DBHandler.Column.notNull("hora_salida", "TIME"),
                    DBHandler.Column.notNull("tarifa", "DOUBLE"),
                    DBHandler.Column.fk("id_empleado", "VARCHAR(10)", "Empleado", "id_empleado")
                )
            );
            
            
            
        } catch (SQLException ex) {
            System.getLogger(DBHandler.class.getName()).log(System.Logger.Level.ERROR, 
                    "", ex);
        }
    }
}
