package sv.edu.udb.rbo.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Crea las tablas ejecutando el script rbo_create.sql, que está en el mismo paquete
 * (el libro guarda shop.sql junto a las clases del modelo).
 *
 * <p>Reemplaza a la antigua clase DBHandler. El script usa CREATE TABLE IF NOT EXISTS, así
 * que ejecutarlo varias veces no tiene efecto. También se puede ejecutar a mano en phpMyAdmin.</p>
 */
public final class EsquemaPeer {

    static final String SCRIPT = "rbo_create.sql";

    private EsquemaPeer() {
    }

    public static void crearTablas(DataManager dataManager) throws SQLException {
        List<String> sentencias = leerSentencias();
        try (Connection connection = dataManager.getConnection();
             Statement s = connection.createStatement()) {
            for (String sql : sentencias) {
                s.execute(sql);
            }
        }
    }

    /**
     * Lee el script y lo divide por ';'. Ignora las líneas de comentario ("--").
     * Es suficiente para un script de solo DDL (ningún texto lleva ';').
     */
    static List<String> leerSentencias() throws SQLException {
        try (InputStream in = EsquemaPeer.class.getResourceAsStream(SCRIPT)) {
            if (in == null) {
                throw new SQLException("No se encontró el script " + SCRIPT + " en el classpath.");
            }
            StringBuilder actual = new StringBuilder();
            List<String> sentencias = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    String recortada = linea.trim();
                    if (recortada.isEmpty() || recortada.startsWith("--")) {
                        continue;
                    }
                    actual.append(linea).append('\n');
                    if (recortada.endsWith(";")) {
                        String sql = actual.toString().trim();
                        sentencias.add(sql.substring(0, sql.length() - 1));
                        actual.setLength(0);
                    }
                }
            }
            if (!actual.toString().isBlank()) {
                sentencias.add(actual.toString().trim());
            }
            return sentencias;
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el script " + SCRIPT, e);
        }
    }
}
