package uci.estructuras;

/** Casilla del arbol binario de busqueda. */
class Nodo<T> {
    T dato;
    Nodo<T> izquierdo;
    Nodo<T> derecho;

    Nodo(T dato) {
        this.dato = dato;
    }
}
