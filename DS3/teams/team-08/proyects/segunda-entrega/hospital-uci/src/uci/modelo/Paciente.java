package uci.modelo;

/**
 * Paciente de la UCI. Equivale a "Estudiante" del sistema Residencias UNAL.
 *
 * Orden natural (compareTo): primero por indice de gravedad creciente
 * (menor indice = mayor urgencia) y, si empatan, por ID. La clave compuesta
 * (indiceGravedad, id) es unica, asi el arbol nunca pierde pacientes.
 */
public class Paciente implements Comparable<Paciente> {

    private final String id;
    private final String nombre;
    private final String diagnostico;
    private int indiceGravedad;
    private int cama; // 0 = sin cama asignada

    public Paciente(String id, String nombre, String diagnostico, int indiceGravedad, int cama) {
        lanzarSiHayError(Validador.validarId(id));
        lanzarSiHayError(Validador.validarNombre(nombre));
        lanzarSiHayError(Validador.validarDiagnostico(diagnostico));
        lanzarSiHayError(Validador.validarIndice(indiceGravedad));
        if (cama < 0 || cama > Cama.MAX) {
            throw new IllegalArgumentException("Número de cama inválido.");
        }
        this.id = id.trim();
        this.nombre = nombre.trim();
        this.diagnostico = diagnostico.trim();
        this.indiceGravedad = indiceGravedad;
        this.cama = cama;
    }

    private static void lanzarSiHayError(String error) {
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getDiagnostico() { return diagnostico; }

    public int getIndiceGravedad() { return indiceGravedad; }

    public int getCama() { return cama; }

    public boolean tieneCama() { return cama > 0; }

    public void setCama(int cama) { this.cama = cama; }

    /**
     * Cambia el indice. IMPORTANTE: el indice es parte de la clave del arbol, por eso
     * solo debe llamarse con el paciente FUERA del arbol (ver SistemaUCI.modificarIndice).
     */
    public void setIndiceGravedad(int indice) {
        lanzarSiHayError(Validador.validarIndice(indice));
        this.indiceGravedad = indice;
    }

    /** Etiqueta de lectura rapida del indice (solo orientativa, proyecto academico). */
    public String nivelUrgencia() {
        if (indiceGravedad <= 25) return "Muy urgente";
        if (indiceGravedad <= 50) return "Urgente";
        if (indiceGravedad <= 75) return "Moderado";
        return "Bajo";
    }

    @Override
    public int compareTo(Paciente otro) {
        int c = Integer.compare(this.indiceGravedad, otro.indiceGravedad);
        return (c != 0) ? c : this.id.compareTo(otro.id);
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " (índice " + indiceGravedad + ")";
    }
}
