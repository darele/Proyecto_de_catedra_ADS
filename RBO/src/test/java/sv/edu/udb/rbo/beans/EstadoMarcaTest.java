package sv.edu.udb.rbo.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Flujo de la marca de asistencia, sin Tomcat ni base de datos. */
class EstadoMarcaTest {

    @Test
    void flujoCompletoDeUnaJornada() {
        EstadoMarca estado = EstadoMarca.SIN_JORNADA;
        assertEquals(AccionMarca.ENTRADA, estado.getSiguienteAccion());
        assertEquals("estado-rojo", estado.getClaseCaja());

        estado = EstadoMarca.despuesDe(estado.getSiguienteAccion());
        assertEquals(EstadoMarca.EN_JORNADA, estado);
        assertEquals(AccionMarca.RECESO, estado.getSiguienteAccion());
        assertTrue(estado.getSiguienteAccion().isBotonAmarillo());

        estado = EstadoMarca.despuesDe(estado.getSiguienteAccion());
        assertEquals(EstadoMarca.EN_RECESO, estado);
        assertEquals("estado-naranja", estado.getClaseCaja());
        assertEquals("Finalizar Receso", estado.getSiguienteAccion().getTextoBoton());

        estado = EstadoMarca.despuesDe(estado.getSiguienteAccion());
        assertEquals(EstadoMarca.DE_VUELTA_DE_RECESO, estado);
        assertEquals("estado-verde", estado.getClaseCaja());
        assertFalse(estado.getSiguienteAccion().isBotonAmarillo());
    }

    @Test
    void laSalidaSiempreTerminaLaJornada() {
        assertEquals(EstadoMarca.SIN_JORNADA, EstadoMarca.despuesDe(AccionMarca.SALIDA));
    }

    @Test
    void elRetornoDeRecesoSeGuardaComoEntrada() {
        assertEquals("entrada", AccionMarca.RETORNO.getTipoMarcaDB());
        assertEquals("receso", AccionMarca.RECESO.getTipoMarcaDB());
        assertEquals("salida", AccionMarca.SALIDA.getTipoMarcaDB());
    }

    @Test
    void desdeParametroIgnoraMayusculasYRechazaValoresDesconocidos() {
        assertEquals(AccionMarca.ENTRADA, AccionMarca.desdeParametro("entrada"));
        assertEquals(AccionMarca.RETORNO, AccionMarca.desdeParametro(" Retorno "));
        assertNull(AccionMarca.desdeParametro("borrarTodo"));
        assertNull(AccionMarca.desdeParametro(null));
    }

    @Test
    void comentariosPorDefecto() {
        assertEquals("Inicio de receso", AccionMarca.RECESO.comentarioPara("  "));
        assertEquals("Retorno de receso", AccionMarca.RETORNO.comentarioPara(null));
        assertEquals("Retorno de receso: almuerzo", AccionMarca.RETORNO.comentarioPara("almuerzo"));
        assertEquals("", AccionMarca.ENTRADA.comentarioPara(null));
    }

    @Test
    void elComentarioSeTruncaAlTamanoDeLaColumna() {
        Marca marca = new Marca("E001", "entrada", "x".repeat(150));
        assertEquals(Marca.MAX_COMENTARIO, marca.getComentario().length());
    }
}
