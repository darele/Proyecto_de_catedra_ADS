package sv.edu.udb.rbo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Comprueba que el script rbo_create.sql se lee y se divide bien en sentencias. */
class EsquemaPeerTest {

    @Test
    void elScriptTieneLasCuatroTablas() throws Exception {
        List<String> sentencias = EsquemaPeer.leerSentencias();
        assertEquals(4, sentencias.size());
        assertTrue(sentencias.get(0).startsWith("CREATE TABLE IF NOT EXISTS Empleado"));
        assertTrue(sentencias.get(1).startsWith("CREATE TABLE IF NOT EXISTS Marca"));
        assertTrue(sentencias.get(2).startsWith("CREATE TABLE IF NOT EXISTS Tarifa"));
        assertTrue(sentencias.get(3).startsWith("CREATE TABLE IF NOT EXISTS Auditoria"));
        for (String sql : sentencias) {
            assertFalse(sql.endsWith(";"), "JDBC no acepta el ';' final: " + sql);
            assertFalse(sql.contains("--"), "Quedó un comentario dentro de la sentencia: " + sql);
        }
    }
}
