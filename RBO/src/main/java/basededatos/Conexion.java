/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package basededatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Conexion {

    private Connection conexion;
    Statement comando;
    private static final String URL = "jdbc:mysql://localhost:3306/rbo_marcas";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Conexion() {
        try {
            conexion = getConnection();
            comando = conexion.createStatement();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo conectar a la base de datos", ex);
        } catch (ClassNotFoundException ex) {
            System.getLogger(Conexion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public void close() {
        try {
            conexion.close();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public ResultSet consultar(String sentencia) {
        ResultSet rs = null;
        try {
            rs = comando.executeQuery(sentencia);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE,
                    "No se pudo ejecutar: " + sentencia, ex);
        }
        return rs;
    }

    public void update(String sentencia) {
        try {
            comando.executeUpdate(sentencia);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE,
                    "No se pudo ejecutar: " + sentencia, ex);
        }
    }
}
