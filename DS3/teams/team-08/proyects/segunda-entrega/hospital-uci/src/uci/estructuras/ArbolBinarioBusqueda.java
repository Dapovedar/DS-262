package uci.estructuras;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * Arbol binario de busqueda (ABB) implementado desde cero.
 *
 * Los menores van a la izquierda y los mayores a la derecha segun compareTo.
 * Sea h la altura del arbol: insertar, buscar y eliminar cuestan O(h)
 * (O(log n) en promedio, O(n) si los datos llegan ya ordenados).
 * El recorrido inorden entrega los elementos de menor a mayor en O(n).
 *
 * Insercion, inorden y altura son iterativos (usan una pila o una cola explicita)
 * para no desbordar la pila de llamadas en arboles muy profundos.
 */
public class ArbolBinarioBusqueda<T extends Comparable<T>> {

    private Nodo<T> raiz;
    private int tamano;

    /** Inserta el elemento. Devuelve false si ya existia. O(h). */
    public boolean insertar(T dato) {
        Objects.requireNonNull(dato, "El elemento no puede ser null.");
        if (raiz == null) {
            raiz = new Nodo<>(dato);
            tamano++;
            return true;
        }
        Nodo<T> actual = raiz;
        while (true) {
            int c = dato.compareTo(actual.dato);
            if (c == 0) {
                return false;
            }
            if (c < 0) {
                if (actual.izquierdo == null) {
                    actual.izquierdo = new Nodo<>(dato);
                    tamano++;
                    return true;
                }
                actual = actual.izquierdo;
            } else {
                if (actual.derecho == null) {
                    actual.derecho = new Nodo<>(dato);
                    tamano++;
                    return true;
                }
                actual = actual.derecho;
            }
        }
    }

    /** Indica si el elemento esta en el arbol. O(h). */
    public boolean contiene(T dato) {
        Nodo<T> actual = raiz;
        while (actual != null && dato != null) {
            int c = dato.compareTo(actual.dato);
            if (c == 0) {
                return true;
            }
            actual = (c < 0) ? actual.izquierdo : actual.derecho;
        }
        return false;
    }

    /** Elimina el elemento. Devuelve false si no existia. O(h). */
    public boolean eliminar(T dato) {
        if (dato == null) {
            return false;
        }
        int antes = tamano;
        raiz = eliminar(raiz, dato);
        return tamano < antes;
    }

    private Nodo<T> eliminar(Nodo<T> nodo, T dato) {
        if (nodo == null) {
            return null;
        }
        int c = dato.compareTo(nodo.dato);
        if (c < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, dato);
        } else if (c > 0) {
            nodo.derecho = eliminar(nodo.derecho, dato);
        } else if (nodo.izquierdo == null || nodo.derecho == null) {
            tamano--; // 0 o 1 hijo: lo reemplaza su unico hijo (o null)
            return (nodo.izquierdo != null) ? nodo.izquierdo : nodo.derecho;
        } else {
            // 2 hijos: se copia el sucesor inorden (menor del subarbol derecho) y se elimina de su lugar
            Nodo<T> sucesor = nodo.derecho;
            while (sucesor.izquierdo != null) {
                sucesor = sucesor.izquierdo;
            }
            nodo.dato = sucesor.dato;
            nodo.derecho = eliminar(nodo.derecho, sucesor.dato); // esta llamada decrementa tamano
        }
        return nodo;
    }

    /** Recorrido inorden (izquierda, nodo, derecha): de menor a mayor. O(n). */
    public List<T> enOrden() {
        List<T> resultado = new ArrayList<>(tamano);
        Deque<Nodo<T>> pila = new ArrayDeque<>();
        Nodo<T> actual = raiz;
        while (actual != null || !pila.isEmpty()) {
            while (actual != null) {
                pila.push(actual);
                actual = actual.izquierdo;
            }
            actual = pila.pop();
            resultado.add(actual.dato);
            actual = actual.derecho;
        }
        return resultado;
    }

    /** Altura del arbol (0 si esta vacio), recorriendo por niveles con una cola. O(n). */
    public int altura() {
        if (raiz == null) {
            return 0;
        }
        Queue<Nodo<T>> cola = new ArrayDeque<>();
        cola.add(raiz);
        int altura = 0;
        while (!cola.isEmpty()) {
            altura++;
            for (int i = cola.size(); i > 0; i--) {
                Nodo<T> n = cola.poll();
                if (n.izquierdo != null) {
                    cola.add(n.izquierdo);
                }
                if (n.derecho != null) {
                    cola.add(n.derecho);
                }
            }
        }
        return altura;
    }

    public int size() { return tamano; }

    public boolean isEmpty() { return tamano == 0; }
}
