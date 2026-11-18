package sv.edu.udb.rbo.beans;

/**
 * Estado de la jornada del empleado. Se guarda en sesión y decide qué se dibuja en
 * MarcaEmpleado.jsp (color de la caja de estado y botón principal).
 *
 * <p>Flujo: SIN_JORNADA -(Entrada)-> EN_JORNADA -(Receso)-> EN_RECESO
 * -(Retorno)-> DE_VUELTA_DE_RECESO -(Entrada)-> EN_JORNADA ...
 * La Salida lleva siempre a SIN_JORNADA.</p>
 *
 * <p>Las vistas leen las propiedades con EL, por ejemplo {@code ${estado.claseCaja}}.</p>
 */
public enum EstadoMarca {

    SIN_JORNADA("estado-rojo",
            "Estado actual: Sin iniciar jornada / Has registrado salida",
            AccionMarca.ENTRADA),
    EN_JORNADA("estado-verde",
            "Estado actual: Has registrado entrada (En jornada)",
            AccionMarca.RECESO),
    EN_RECESO("estado-naranja",
            "Estado actual: Estás en receso",
            AccionMarca.RETORNO),
    DE_VUELTA_DE_RECESO("estado-verde",
            "Estado actual: Has registrado entrada (Retorno de receso)",
            AccionMarca.ENTRADA);

    private final String claseCaja;
    private final String textoCaja;
    private final AccionMarca siguienteAccion;

    EstadoMarca(String claseCaja, String textoCaja, AccionMarca siguienteAccion) {
        this.claseCaja = claseCaja;
        this.textoCaja = textoCaja;
        this.siguienteAccion = siguienteAccion;
    }

    /** Estado que queda después de registrar {@code accion}. */
    public static EstadoMarca despuesDe(AccionMarca accion) {
        switch (accion) {
            case ENTRADA:
                return EN_JORNADA;
            case RECESO:
                return EN_RECESO;
            case RETORNO:
                return DE_VUELTA_DE_RECESO;
            case SALIDA:
            default:
                return SIN_JORNADA;
        }
    }

    /** Clase CSS de la caja de estado (definida en css/rbo.css). */
    public String getClaseCaja() {
        return claseCaja;
    }

    public String getTextoCaja() {
        return textoCaja;
    }

    /** Acción que registra el botón principal en este estado. */
    public AccionMarca getSiguienteAccion() {
        return siguienteAccion;
    }
}
