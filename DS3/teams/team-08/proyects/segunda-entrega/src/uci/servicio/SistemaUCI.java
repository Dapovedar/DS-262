package uci.servicio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import uci.estructuras.ArbolBinarioBusqueda;
import uci.modelo.Cama;
import uci.modelo.Paciente;
import uci.persistencia.RepositorioCSV;

/**
 * Logica del Sistema de Asignacion Prioritaria de Camas UCI.
 * Equivale a SistemaResidencias del proyecto Residencias UNAL.
 *
 * Estructuras sincronizadas sobre los mismos objetos Paciente:
 *  - HashMap ID -> Paciente: existencia y acceso directo por ID en O(1) promedio.
 *  - ArbolBinarioBusqueda ordenado por (indiceGravedad, id): lista de prioridad.
 *  - Cola (Queue) de camas libres: reparte las camas disponibles en O(1) cada una.
 *
 * Si se entrega un RepositorioCSV, cada cambio se guarda en disco.
 */
public class SistemaUCI {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HashMap<String, Paciente> porId = new HashMap<>();
    private final ArbolBinarioBusqueda<Paciente> arbol = new ArbolBinarioBusqueda<>();
    private final RepositorioCSV repositorio; // puede ser null (pruebas en memoria)
    private int camas = 10;
    private boolean cambiosPendientes = false;
    private String ultimaAsignacion = "Sin asignaciones todavía";

    /** Sistema en memoria, sin persistencia. */
    public SistemaUCI() {
        this.repositorio = null;
    }

    /** Sistema que carga y guarda sus datos con el repositorio. */
    public SistemaUCI(RepositorioCSV repositorio) {
        this.repositorio = repositorio;
        try {
            for (Paciente p : repositorio.cargarPacientes()) {
                if (!porId.containsKey(p.getId())) {
                    porId.put(p.getId(), p);
                    arbol.insertar(p);
                }
            }
            Properties cfg = repositorio.cargarConfig();
            camas = Integer.parseInt(cfg.getProperty("camas", String.valueOf(camas)));
            cambiosPendientes = Boolean.parseBoolean(cfg.getProperty("pendientes", "false"));
            ultimaAsignacion = cfg.getProperty("ultimaAsignacion", ultimaAsignacion);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la base de datos.", e);
        }
    }

    // ------------------------------------------------------------ operaciones

    /** Registra un paciente nuevo. O(log n) en promedio (HashMap O(1) + ABB O(h)). */
    public Paciente registrar(String id, String nombre, String diagnostico, int indice) {
        Paciente nuevo = new Paciente(id, nombre, diagnostico, indice, 0); // valida los datos
        if (porId.containsKey(nuevo.getId())) {
            throw new IllegalArgumentException(
                    "El ID " + nuevo.getId() + " ya existe en la base de datos de pacientes.");
        }
        porId.put(nuevo.getId(), nuevo);
        arbol.insertar(nuevo);
        cambiosPendientes = true;
        persistir();
        return nuevo;
    }

    /** Acceso directo por ID. O(1) promedio. Devuelve null si no existe. */
    public Paciente buscar(String id) {
        return (id == null) ? null : porId.get(id.trim());
    }

    public boolean existe(String id) {
        return buscar(id) != null;
    }

    /** Lista de prioridad: de menor a mayor indice (mas urgente primero). O(n). */
    public List<Paciente> listarOrdenado() {
        return arbol.enOrden();
    }

    /**
     * Cambia el indice de gravedad y reubica al paciente. O(h).
     * Orden obligatorio: sacar del arbol con la clave VIEJA, cambiar, reinsertar.
     */
    public Paciente modificarIndice(String id, int nuevoIndice) {
        Paciente p = obtenerOFallar(id);
        String error = uci.modelo.Validador.validarIndice(nuevoIndice);
        if (error != null) {
            throw new IllegalArgumentException(error); // se valida antes de tocar el arbol
        }
        arbol.eliminar(p);
        p.setIndiceGravedad(nuevoIndice);
        arbol.insertar(p);
        cambiosPendientes = true;
        persistir();
        return p;
    }

    /** Alta medica: elimina al paciente del HashMap y del arbol. O(h). */
    public Paciente darAlta(String id) {
        Paciente p = obtenerOFallar(id);
        arbol.eliminar(p);
        porId.remove(p.getId());
        cambiosPendientes = true;
        persistir();
        return p;
    }

    /** Define cuantas camas estan disponibles (1 a Cama.MAX). O(1). */
    public void definirCamas(int n) {
        String error = uci.modelo.Validador.validarCamas(n);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        camas = n;
        cambiosPendientes = true;
        persistir();
    }

    /**
     * Asigna las camas a los N pacientes de menor indice. O(n).
     * Un paciente que ya tiene cama y sigue entre los N primeros la conserva (no se le cambia
     * de cama); los nuevos reciben camas libres tomadas de una cola.
     */
    public ResultadoAsignacion asignar() {
        List<Paciente> orden = arbol.enOrden();
        int n = Math.min(camas, orden.size());
        List<Paciente> ganadores = new ArrayList<>(orden.subList(0, n));
        Set<String> idsGanadores = new HashSet<>();
        for (Paciente p : ganadores) {
            idsGanadores.add(p.getId());
        }

        // 1. Liberar camas de quienes salieron del grupo (o con numero fuera del rango actual)
        boolean[] ocupada = new boolean[camas + 1];
        for (Paciente p : orden) {
            if (p.tieneCama()) {
                if (idsGanadores.contains(p.getId()) && p.getCama() <= camas) {
                    ocupada[p.getCama()] = true;
                } else {
                    p.setCama(0);
                }
            }
        }
        // 2. Cola de camas libres
        Queue<Integer> libres = new ArrayDeque<>();
        for (int i = 1; i <= camas; i++) {
            if (!ocupada[i]) {
                libres.add(i);
            }
        }
        // 3. Los ganadores sin cama la reciben, en orden de prioridad
        for (Paciente p : ganadores) {
            if (!p.tieneCama()) {
                p.setCama(libres.poll());
            }
        }
        cambiosPendientes = false;
        ultimaAsignacion = LocalDateTime.now().format(FORMATO_FECHA);
        persistir();
        return new ResultadoAsignacion(conCama(), enEspera());
    }

    // ------------------------------------------------------------ consultas

    /** Pacientes con cama asignada, por orden de prioridad. O(n). */
    public List<Paciente> conCama() {
        List<Paciente> r = new ArrayList<>();
        for (Paciente p : arbol.enOrden()) {
            if (p.tieneCama()) {
                r.add(p);
            }
        }
        return r;
    }

    /** Pacientes en lista de espera (sin cama), por orden de prioridad. O(n). */
    public List<Paciente> enEspera() {
        List<Paciente> r = new ArrayList<>();
        for (Paciente p : arbol.enOrden()) {
            if (!p.tieneCama()) {
                r.add(p);
            }
        }
        return r;
    }

    /** Posicion (desde 1) del paciente en la lista de prioridad; 0 si no existe. O(n). */
    public int posicion(String id) {
        Paciente buscado = buscar(id);
        if (buscado == null) {
            return 0;
        }
        List<Paciente> orden = arbol.enOrden();
        for (int i = 0; i < orden.size(); i++) {
            if (orden.get(i) == buscado) {
                return i + 1;
            }
        }
        return 0;
    }

    public int cantidad() { return porId.size(); }

    public int getCamas() { return camas; }

    public boolean isCambiosPendientes() { return cambiosPendientes; }

    public String getUltimaAsignacion() { return ultimaAsignacion; }

    /** Altura del ABB (sirve para analizar el costo real de las operaciones). */
    public int alturaArbol() { return arbol.altura(); }

    /** Verifica que HashMap y arbol tengan la misma cantidad de pacientes (para pruebas). */
    public boolean esConsistente() {
        return porId.size() == arbol.size();
    }

    // ------------------------------------------------------------ internos

    private Paciente obtenerOFallar(String id) {
        Paciente p = buscar(id);
        if (p == null) {
            throw new IllegalArgumentException("El ID " + id + " no existe en la base de datos de pacientes.");
        }
        return p;
    }

    private void persistir() {
        if (repositorio == null) {
            return;
        }
        try {
            repositorio.guardarPacientes(arbol.enOrden());
            Properties cfg = new Properties();
            cfg.setProperty("camas", String.valueOf(camas));
            cfg.setProperty("pendientes", String.valueOf(cambiosPendientes));
            cfg.setProperty("ultimaAsignacion", ultimaAsignacion);
            repositorio.guardarConfig(cfg);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar en la base de datos.", e);
        }
    }
}
