package uci.modelo;

/** Utilidades para nombrar y ubicar las camas de la UCI (numeradas desde 1). */
public final class Cama {

    /** Capacidad fisica maxima de la unidad. */
    public static final int MAX = 30;

    private Cama() { }

    /** Codigo de la cama, por ejemplo UCI-03. */
    public static String codigo(int numero) {
        return String.format("UCI-%02d", numero);
    }

    /** Ubicacion fisica: 6 camas por sala y 12 camas por piso (desde el piso 4). */
    public static String ubicacion(int numero) {
        int piso = 4 + (numero - 1) / 12;
        char sala = (char) ('A' + (numero - 1) / 6);
        return "Piso " + piso + ", Sala " + sala;
    }

    public static String descripcion(int numero) {
        return "Cama " + codigo(numero) + " - " + ubicacion(numero);
    }
}
