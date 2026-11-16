package uci;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import uci.estructuras.ArbolBinarioBusqueda;
import uci.modelo.Paciente;

/**
 * Resultados preliminares: tiempos y altura del arbol segun el tamano de la entrada,
 * con indices aleatorios (caso promedio) y ya ordenados (peor caso del ABB).
 * Ejecutar con: java -cp out uci.Benchmark
 */
public class Benchmark {

    public static void main(String[] args) {
        calentar();
        System.out.println("| Tipo de datos | n | Altura ABB | log2(n) | Insertar n (ms) | Inorden (ms) | Búsq. por ID, 1000 (µs) |");
        System.out.println("|---|---:|---:|---:|---:|---:|---:|");
        for (int n : new int[] {1000, 10000, 100000}) {
            medir("Aleatorios", n, false);
        }
        for (int n : new int[] {1000, 5000, 20000}) {
            medir("Ya ordenados", n, true);
        }
    }

    private static void medir(String tipo, int n, boolean ordenados) {
        Paciente[] datos = generar(n, ordenados);
        ArbolBinarioBusqueda<Paciente> arbol = new ArbolBinarioBusqueda<>();
        HashMap<String, Paciente> mapa = new HashMap<>();

        long t0 = System.nanoTime();
        for (Paciente p : datos) {
            arbol.insertar(p);
            mapa.put(p.getId(), p);
        }
        double insertar = (System.nanoTime() - t0) / 1e6;

        t0 = System.nanoTime();
        List<Paciente> lista = arbol.enOrden();
        double inorden = (System.nanoTime() - t0) / 1e6;

        Random r = new Random(7);
        t0 = System.nanoTime();
        int encontrados = 0;
        for (int i = 0; i < 1000; i++) {
            if (mapa.get(datos[r.nextInt(n)].getId()) != null) {
                encontrados++;
            }
        }
        double busqueda = (System.nanoTime() - t0) / 1e3;

        if (lista.size() != n || encontrados != 1000) {
            throw new IllegalStateException("Resultado inconsistente");
        }
        System.out.printf("| %s | %d | %d | %.1f | %.1f | %.2f | %.1f |%n", tipo, n, arbol.altura(),
                Math.log(n) / Math.log(2), insertar, inorden, busqueda);
    }

    /** Genera n pacientes con ID unico. Indice 0..100 aleatorio, o creciente si ordenados. */
    private static Paciente[] generar(int n, boolean ordenados) {
        Random r = new Random(42);
        Paciente[] datos = new Paciente[n];
        for (int i = 0; i < n; i++) {
            String id = String.valueOf(10000000 + i);
            int indice = ordenados ? Math.min(100, i * 101 / n) : r.nextInt(101);
            datos[i] = new Paciente(id, "Paciente Prueba", "Neumonía grave", indice, 0);
        }
        if (!ordenados) {
            for (int i = n - 1; i > 0; i--) { // barajar los IDs para que no lleguen ordenados
                int j = r.nextInt(i + 1);
                Paciente t = datos[i];
                datos[i] = datos[j];
                datos[j] = t;
            }
        }
        return datos;
    }

    private static void calentar() {
        for (int k = 0; k < 3; k++) {
            ArbolBinarioBusqueda<Paciente> a = new ArbolBinarioBusqueda<>();
            for (Paciente p : generar(5000, false)) {
                a.insertar(p);
            }
            a.enOrden();
        }
    }
}
