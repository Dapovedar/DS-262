package uci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uci.estructuras.ArbolBinarioBusqueda;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.modelo.Validador;
import uci.persistencia.RepositorioCSV;
import uci.servicio.Autenticacion;
import uci.servicio.ResultadoAsignacion;
import uci.servicio.Seguridad;
import uci.servicio.SistemaUCI;

/**
 * Pruebas automaticas (sin librerias externas). Ejecutar con pruebas.bat / pruebas.sh
 * o: java -cp out uci.PruebasSistema
 */
public class PruebasSistema {

    private static int ok = 0;
    private static int fallos = 0;

    public static void main(String[] args) throws Exception {
        pruebasArbol();
        pruebasValidador();
        pruebasSistema();
        pruebasAsignacion();
        pruebasSeguridadYLogin();
        pruebasPersistencia();
        pruebasBaseDeDatosInicial();
        System.out.println("\n=== RESUMEN: " + ok + " pruebas correctas, " + fallos + " fallidas ===");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------ utilidades

    private static void verificar(String nombre, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("  OK     " + nombre);
        } else {
            fallos++;
            System.out.println("  FALLA  " + nombre);
        }
    }

    private static void lanza(String nombre, Runnable accion) {
        try {
            accion.run();
            verificar(nombre + " (debía lanzar error)", false);
        } catch (IllegalArgumentException e) {
            verificar(nombre, true);
        }
    }

    private static void titulo(String t) {
        System.out.println("\n[" + t + "]");
    }

    private static String ids(List<Paciente> l) {
        StringBuilder sb = new StringBuilder();
        for (Paciente p : l) {
            sb.append(p.getId()).append(' ');
        }
        return sb.toString().trim();
    }

    private static Paciente p(String id, int indice) {
        return new Paciente(id, "Paciente Prueba", "Neumonía grave", indice, 0);
    }

    // ------------------------------------------------------------ arbol

    private static void pruebasArbol() {
        titulo("Árbol binario de búsqueda");
        ArbolBinarioBusqueda<Integer> a = new ArbolBinarioBusqueda<>();
        verificar("árbol vacío: altura 0 y tamaño 0", a.altura() == 0 && a.size() == 0 && a.isEmpty());
        for (int x : new int[] {50, 30, 70, 20, 40, 60, 80, 35, 45}) {
            a.insertar(x);
        }
        verificar("inorden devuelve los datos ordenados", a.enOrden().equals(List.of(20, 30, 35, 40, 45, 50, 60, 70, 80)));
        verificar("no admite repetidos", !a.insertar(50) && a.size() == 9);
        verificar("contiene encuentra existentes y descarta ausentes", a.contiene(45) && !a.contiene(99));
        verificar("eliminar hoja", a.eliminar(20) && !a.contiene(20));
        verificar("eliminar nodo con un hijo", a.eliminar(60) && a.enOrden().equals(List.of(30, 35, 40, 45, 50, 70, 80)));
        verificar("eliminar nodo con dos hijos (raíz)", a.eliminar(50) && a.enOrden().equals(List.of(30, 35, 40, 45, 70, 80)));
        verificar("eliminar inexistente devuelve false", !a.eliminar(1234) && a.size() == 6);

        ArbolBinarioBusqueda<Integer> ordenado = new ArbolBinarioBusqueda<>();
        for (int i = 1; i <= 100; i++) {
            ordenado.insertar(i);
        }
        verificar("datos ya ordenados degeneran el árbol (altura = n)", ordenado.altura() == 100);

        ArbolBinarioBusqueda<Paciente> ap = new ArbolBinarioBusqueda<>();
        ap.insertar(p("200000", 40));
        ap.insertar(p("100000", 40));
        ap.insertar(p("300000", 10));
        verificar("empate de índice se desempata por ID", ids(ap.enOrden()).equals("300000 100000 200000"));
    }

    // ------------------------------------------------------------ validaciones

    private static void pruebasValidador() {
        titulo("Validación de campos");
        verificar("ID válido", Validador.validarId("1012345678") == null);
        verificar("ID con letras rechazado", Validador.validarId("12ab45") != null);
        verificar("ID muy corto rechazado", Validador.validarId("1234") != null);
        verificar("ID muy largo rechazado", Validador.validarId("1234567890123") != null);
        verificar("ID vacío rechazado", Validador.validarId("  ") != null);
        verificar("nombre con tildes y eñe válido", Validador.validarNombre("María Ñúñez Pérez") == null);
        verificar("nombre con números rechazado", Validador.validarNombre("Maria 2") != null);
        verificar("nombre con símbolos rechazado", Validador.validarNombre("Ana@Gomez") != null);
        verificar("nombre muy corto rechazado", Validador.validarNombre("Al") != null);
        verificar("diagnóstico con números rechazado", Validador.validarDiagnostico("Fractura 3") != null);
        verificar("índice 0 y 100 válidos", Validador.validarIndice(0) == null && Validador.validarIndice(100) == null);
        verificar("índice -1 y 101 rechazados", Validador.validarIndice(-1) != null && Validador.validarIndice(101) != null);
        verificar("índice en texto: letras rechazadas", Validador.validarIndiceTexto("5a") != null);
        verificar("índice en texto: vacío rechazado", Validador.validarIndiceTexto("") != null);
        verificar("camas 0 y 31 rechazadas", Validador.validarCamas(0) != null && Validador.validarCamas(31) != null);
        verificar("camas 1 y 30 válidas", Validador.validarCamas(1) == null && Validador.validarCamas(30) == null);
        verificar("clave corta rechazada", Validador.validarClave("abc") != null);
    }

    // ------------------------------------------------------------ sistema

    private static SistemaUCI sistemaBase() {
        SistemaUCI s = new SistemaUCI();
        s.registrar("1000001", "Ana María Torres", "Choque séptico", 30);
        s.registrar("1000002", "Beto Ramírez Gil", "Neumonía grave", 10);
        s.registrar("1000003", "Carla Pérez Soto", "Politraumatismo", 20);
        s.registrar("1000004", "Diego Mora Luna", "Infarto agudo de miocardio", 10); // empata con Beto
        return s;
    }

    private static void pruebasSistema() {
        titulo("Sistema UCI: registro, búsqueda, modificación y alta");
        SistemaUCI s = sistemaBase();
        verificar("lista ordenada con desempate por ID", ids(s.listarOrdenado()).equals("1000002 1000004 1000003 1000001"));
        lanza("registrar ID repetido", () -> s.registrar("1000001", "Otro Nombre", "Fractura expuesta", 5));
        lanza("registrar con nombre inválido", () -> s.registrar("1000009", "Nombre123", "Fractura expuesta", 5));
        lanza("registrar con índice fuera de rango", () -> s.registrar("1000009", "Nombre Valido", "Fractura expuesta", 150));
        verificar("tras errores no se alteró la cantidad", s.cantidad() == 4 && s.esConsistente());
        verificar("buscar por ID existente", s.buscar("1000003") != null && s.existe("1000003"));
        verificar("buscar por ID inexistente devuelve null", s.buscar("9999999") == null && !s.existe("9999999"));

        int antes = s.posicion("1000001");
        s.modificarIndice("1000001", 1);
        verificar("empeoramiento: el paciente sube a la posición 1", antes == 4 && s.posicion("1000001") == 1);
        s.modificarIndice("1000001", 99);
        verificar("mejoría: el paciente baja a la última posición", s.posicion("1000001") == 4);
        lanza("modificar con índice inválido no daña el árbol", () -> s.modificarIndice("1000001", -5));
        verificar("estructuras consistentes tras modificar", ids(s.listarOrdenado()).equals("1000002 1000004 1000003 1000001") && s.esConsistente());
        lanza("modificar ID inexistente", () -> s.modificarIndice("9999999", 5));

        s.darAlta("1000002");
        verificar("alta elimina de la base y del árbol", !s.existe("1000002") && ids(s.listarOrdenado()).equals("1000004 1000003 1000001"));
        lanza("alta de ID inexistente", () -> s.darAlta("1000002"));
        verificar("hay cambios pendientes tras modificar", s.isCambiosPendientes());
    }

    private static void pruebasAsignacion() {
        titulo("Asignación de camas");
        SistemaUCI s = sistemaBase();
        s.definirCamas(2);
        ResultadoAsignacion r = s.asignar();
        verificar("las 2 camas van a los de menor índice", ids(r.conCama()).equals("1000002 1000004"));
        verificar("el resto queda en espera", ids(r.enEspera()).equals("1000003 1000001"));
        verificar("asignar limpia los cambios pendientes", !s.isCambiosPendientes());
        int camaBeto = s.buscar("1000002").getCama();
        int camaDiego = s.buscar("1000004").getCama();
        verificar("las camas asignadas son distintas y válidas", camaBeto != camaDiego && camaBeto >= 1 && camaDiego >= 1);

        s.asignar();
        verificar("reasignar sin cambios conserva las mismas camas",
                s.buscar("1000002").getCama() == camaBeto && s.buscar("1000004").getCama() == camaDiego);

        s.registrar("1000005", "Elena Rojas Vega", "Choque séptico", 1); // el más grave
        s.asignar();
        verificar("un paciente más grave desplaza al de menor prioridad",
                s.buscar("1000005").tieneCama() && s.buscar("1000002").tieneCama() && !s.buscar("1000004").tieneCama());
        verificar("el paciente que conserva cama no se mueve", s.buscar("1000002").getCama() == camaBeto);
        verificar("el nuevo ocupa la cama liberada", s.buscar("1000005").getCama() == camaDiego);

        s.darAlta("1000005");
        s.asignar();
        verificar("tras el alta, entra el siguiente en la lista", s.buscar("1000004").tieneCama());

        s.definirCamas(30);
        r = s.asignar();
        verificar("más camas que pacientes: todos tienen cama", r.enEspera().isEmpty() && r.conCama().size() == s.cantidad());
        s.definirCamas(1);
        r = s.asignar();
        verificar("al reducir camas solo queda 1 paciente con cama", r.conCama().size() == 1 && r.enEspera().size() == s.cantidad() - 1);
        lanza("definir 0 camas", () -> s.definirCamas(0));
        lanza("definir 31 camas", () -> s.definirCamas(31));
        int posicion = s.posicion("1000001");
        verificar("posición de un paciente en la lista", posicion >= 1 && posicion <= s.cantidad());
    }

    // ------------------------------------------------------------ seguridad y login

    private static void pruebasSeguridadYLogin() {
        titulo("Seguridad e inicio de sesión");
        String guardada = Seguridad.proteger("MiClave2026");
        verificar("la clave no se guarda en texto plano", !guardada.contains("MiClave2026"));
        verificar("clave correcta verifica", Seguridad.verificar("MiClave2026", guardada));
        verificar("clave incorrecta no verifica", !Seguridad.verificar("miclave2026", guardada));
        verificar("dos hashes de la misma clave difieren (salt)", !guardada.equals(Seguridad.proteger("MiClave2026")));

        Funcionario f = new Funcionario("1010101010", "Laura Gómez", "Médica intensivista", guardada);
        Autenticacion auth = new Autenticacion(List.of(f));
        SistemaUCI s = sistemaBase();
        verificar("funcionario con clave correcta ingresa", auth.ingresarFuncionario("1010101010", "MiClave2026") == f);
        lanza("funcionario con ID inexistente", () -> auth.ingresarFuncionario("2020202020", "MiClave2026"));
        lanza("funcionario con clave incorrecta (1)", () -> auth.ingresarFuncionario("1010101010", "mala1"));
        lanza("funcionario con clave incorrecta (2)", () -> auth.ingresarFuncionario("1010101010", "mala2"));
        lanza("funcionario con clave incorrecta (3)", () -> auth.ingresarFuncionario("1010101010", "mala3"));
        lanza("cuenta bloqueada tras 3 fallos (incluso con clave correcta)", () -> auth.ingresarFuncionario("1010101010", "MiClave2026"));
        verificar("paciente existente ingresa solo con ID", auth.ingresarPaciente(s, "1000001") != null);
        lanza("paciente con ID inexistente", () -> auth.ingresarPaciente(s, "9999999"));
        lanza("paciente con ID inválido", () -> auth.ingresarPaciente(s, "abc"));
    }

    // ------------------------------------------------------------ persistencia

    private static void pruebasPersistencia() throws IOException {
        titulo("Persistencia en archivos (base de datos)");
        Path tmp = Files.createTempDirectory("uci-prueba");
        RepositorioCSV repo = new RepositorioCSV(tmp);
        SistemaUCI s = new SistemaUCI(repo);
        verificar("base vacía al inicio", s.cantidad() == 0);
        s.registrar("1000001", "Ana María Torres", "Choque séptico", 30);
        s.registrar("1000002", "Beto Ramírez Gil", "Neumonía grave", 10);
        s.definirCamas(1);
        s.asignar();
        s.modificarIndice("1000001", 5);

        SistemaUCI recargado = new SistemaUCI(new RepositorioCSV(tmp));
        verificar("al reiniciar se recuperan los pacientes", recargado.cantidad() == 2);
        verificar("se conserva el índice modificado", recargado.buscar("1000001").getIndiceGravedad() == 5);
        verificar("se conserva el orden de la lista", ids(recargado.listarOrdenado()).equals(ids(s.listarOrdenado())));
        verificar("se conserva la cantidad de camas", recargado.getCamas() == 1);
        verificar("se conserva la cama asignada", recargado.buscar("1000002").getCama() == s.buscar("1000002").getCama());
        verificar("se conservan los cambios pendientes", recargado.isCambiosPendientes());
        recargado.darAlta("1000002");
        verificar("el alta también se guarda", new SistemaUCI(new RepositorioCSV(tmp)).cantidad() == 1);
    }

    // ------------------------------------------------------------ base de datos de ejemplo

    private static void pruebasBaseDeDatosInicial() throws IOException {
        titulo("Base de datos de ejemplo (carpeta data/)");
        Path origen = Path.of("data");
        if (!Files.isDirectory(origen)) {
            System.out.println("  (se omite: no se encontró la carpeta data/; ejecute desde la carpeta del proyecto)");
            return;
        }
        Path copia = Files.createTempDirectory("uci-datos"); // se trabaja sobre una copia para no alterar data/
        for (String nombre : new String[] {"pacientes.csv", "funcionarios.csv", "config.properties"}) {
            Files.copy(origen.resolve(nombre), copia.resolve(nombre));
        }
        RepositorioCSV repo = new RepositorioCSV(copia);
        SistemaUCI s = new SistemaUCI(repo);
        Autenticacion auth = new Autenticacion(repo.cargarFuncionarios());
        verificar("hay 50 pacientes cargados", s.cantidad() == 50);
        verificar("no se omitió ninguna línea del archivo", repo.getLineasOmitidas() == 0);
        verificar("hay 5 funcionarios cargados", auth.cantidadFuncionarios() == 5);
        Set<String> ids = new HashSet<>();
        int empates = 0;
        List<Paciente> orden = s.listarOrdenado();
        for (int i = 0; i < orden.size(); i++) {
            ids.add(orden.get(i).getId());
            if (i > 0 && orden.get(i).getIndiceGravedad() == orden.get(i - 1).getIndiceGravedad()) {
                empates++;
            }
        }
        verificar("los 50 IDs son únicos", ids.size() == 50);
        verificar("hay pacientes con el mismo índice (prueba de empates)", empates > 0);
        verificar("hay 12 camas y 12 pacientes con cama", s.getCamas() == 12 && s.conCama().size() == 12);
        List<Paciente> antes = s.conCama();
        s.asignar();
        verificar("la asignación inicial es consistente (reasignar no cambia nada)", ids(antes).equals(ids(s.conCama())));
        verificar("el funcionario de ejemplo ingresa con su clave",
                auth.ingresarFuncionario("1010101010", "Uci#2026a").getNombre().startsWith("Laura"));
    }
}
