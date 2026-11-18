package sv.edu.udb.rbo.rh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sv.edu.udb.rbo.ModoDesarrollo;
import sv.edu.udb.rbo.beans.Empleado;
import sv.edu.udb.rbo.beans.Tarifa;

/**
 * Pruebas de la Interfaz de RH, desacopladas de Tomcat y de la base de datos.
 * Agregue aquí un caso por cada action nuevo que registre en RHServlet.resolverVista.
 */
class RHServletTest {

    @Test
    void sinActionOConActionDesconocidoMuestraLaPortada() {
        assertEquals(RHServlet.VISTA_INICIO, RHServlet.resolverVista(null));
        assertEquals(RHServlet.VISTA_INICIO, RHServlet.resolverVista("inicio"));
        assertEquals(RHServlet.VISTA_INICIO, RHServlet.resolverVista("noExiste"));
    }

    @Test
    void actionsMuestranSuVista() {
        assertEquals(RHServlet.VISTA_EMPLEADOS, RHServlet.resolverVista("empleados"));
        assertEquals(RHServlet.VISTA_TARIFAS, RHServlet.resolverVista("tarifas"));
    }

    @Test
    void soloRHYAdminPuedenEntrar() {
        assertTrue(RHServlet.puedeEntrar(new Empleado("R1", "Rita", "Gómez", "RH")));
        assertTrue(RHServlet.puedeEntrar(new Empleado("A1", "Ana", "López", "admin")));
        assertTrue(RHServlet.puedeEntrar(ModoDesarrollo.rhDePrueba()));
        assertFalse(RHServlet.puedeEntrar(new Empleado("E1", "Luis", "Pérez", "Empleado")));
        assertFalse(RHServlet.puedeEntrar(new Empleado("C1", "Carla", "Ruiz", "Contabilidad")));
        assertFalse(RHServlet.puedeEntrar(null));
    }

    @Test
    void tarifaValidaArmaElBean() {
        Tarifa t = RHServlet.validarTarifa(" E001 ", "2026-11-18", "08:00", "17:00", "4.50");
        assertEquals("E001", t.getIdEmpleado());
        assertEquals("2026-11-18", t.getFechaTarifa().toString());
        assertEquals("08:00:00", t.getHoraEntrada().toString());
        assertEquals("17:00:00", t.getHoraSalida().toString());
        assertEquals(4.50, t.getTarifa());
    }

    @Test
    void tarifaInvalidaSeRechaza() {
        // carnet
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("", "2026-11-18", "08:00", "17:00", "4.50"));
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("12345678901", "2026-11-18", "08:00", "17:00", "4.50"));
        // monto
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("E001", "2026-11-18", "08:00", "17:00", "abc"));
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("E001", "2026-11-18", "08:00", "17:00", "0"));
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("E001", "2026-11-18", "08:00", "17:00", "NaN"));
        // fecha y horario
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("E001", "18/11/2026", "08:00", "17:00", "4.50"));
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("E001", "2026-11-18", "17:00", "08:00", "4.50"));
        assertThrows(IllegalArgumentException.class,
                () -> RHServlet.validarTarifa("E001", "2026-11-18", "08:00", "08:00", "4.50"));
    }
}
