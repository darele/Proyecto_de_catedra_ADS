package sv.edu.udb.rbo.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sv.edu.udb.rbo.ModoDesarrollo;
import sv.edu.udb.rbo.beans.Empleado;

/**
 * Pruebas de la Interfaz de Admin, desacopladas de Tomcat y de la base de datos.
 * Agregue aquí un caso por cada action nuevo que registre en AdminServlet.resolverVista.
 */
class AdminServletTest {

    @Test
    void sinActionOConActionDesconocidoMuestraLaPortada() {
        assertEquals(AdminServlet.VISTA_INICIO, AdminServlet.resolverVista(null));
        assertEquals(AdminServlet.VISTA_INICIO, AdminServlet.resolverVista("inicio"));
        assertEquals(AdminServlet.VISTA_INICIO, AdminServlet.resolverVista("noExiste"));
    }

    @Test
    void actionEmpleadosMuestraLaLista() {
        assertEquals(AdminServlet.VISTA_EMPLEADOS, AdminServlet.resolverVista("empleados"));
    }

    @Test
    void soloElRolAdminPuedeEntrar() {
        assertTrue(AdminServlet.puedeEntrar(new Empleado("A1", "Ana", "López", "admin")));
        assertTrue(AdminServlet.puedeEntrar(ModoDesarrollo.adminDePrueba()));
        assertFalse(AdminServlet.puedeEntrar(new Empleado("E1", "Luis", "Pérez", "Empleado")));
        assertFalse(AdminServlet.puedeEntrar(new Empleado("R1", "Rita", "Gómez", "RH")));
        assertFalse(AdminServlet.puedeEntrar(null));
    }
}
