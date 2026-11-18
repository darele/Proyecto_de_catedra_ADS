package sv.edu.udb.rbo.beans;

/**
 * Acciones que el empleado puede registrar desde la pantalla de marca.
 *
 * <p>Cada acción sabe qué valor del ENUM {@code tipo_marca} guardar en la base, qué
 * comentario usar por defecto y qué mensaje mostrar. Antes esta lógica estaba repartida
 * entre MarcaServlet y un scriptlet de marcaEmpleado.jsp.</p>
 */
public enum AccionMarca {

    ENTRADA("Entrada", "entrada", "Entrada", false,
            "Marca de entrada registrada correctamente."),
    RECESO("Receso", "receso", "Receso", true,
            "Entrada a receso registrada correctamente."),
    RETORNO("Retorno", "entrada", "Finalizar Receso", true,
            "Retorno de receso registrado correctamente."),
    SALIDA("Salida", "salida", "Salida", false,
            "Marca de salida registrada correctamente.");

    private final String parametro;
    private final String tipoMarcaDB;
    private final String textoBoton;
    private final boolean botonAmarillo;
    private final String mensajeConfirmacion;

    AccionMarca(String parametro, String tipoMarcaDB, String textoBoton,
            boolean botonAmarillo, String mensajeConfirmacion) {
        this.parametro = parametro;
        this.tipoMarcaDB = tipoMarcaDB;
        this.textoBoton = textoBoton;
        this.botonAmarillo = botonAmarillo;
        this.mensajeConfirmacion = mensajeConfirmacion;
    }

    /**
     * Convierte el parámetro {@code tipo_marca} del formulario en una acción.
     *
     * @return la acción, o {@code null} si el valor no es válido. El código original
     *         registraba una SALIDA ante cualquier valor desconocido; ahora se ignora.
     */
    public static AccionMarca desdeParametro(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        for (AccionMarca accion : values()) {
            if (accion.parametro.equalsIgnoreCase(limpio)) {
                return accion;
            }
        }
        return null;
    }

    /** Comentario a guardar: aplica el texto por defecto de cada acción (como el código original). */
    public String comentarioPara(String comentarioUsuario) {
        String c = (comentarioUsuario == null) ? "" : comentarioUsuario.trim();
        switch (this) {
            case RECESO:
                return c.isEmpty() ? "Inicio de receso" : c;
            case RETORNO:
                return c.isEmpty() ? "Retorno de receso" : "Retorno de receso: " + c;
            default:
                return c;
        }
    }

    /** Valor que viaja en el formulario ({@code name="tipo_marca"}). */
    public String getParametro() {
        return parametro;
    }

    /** Valor para la columna {@code Marca.tipo_marca}. */
    public String getTipoMarcaDB() {
        return tipoMarcaDB;
    }

    public String getTextoBoton() {
        return textoBoton;
    }

    public boolean isBotonAmarillo() {
        return botonAmarillo;
    }

    public String getMensajeConfirmacion() {
        return mensajeConfirmacion;
    }
}
