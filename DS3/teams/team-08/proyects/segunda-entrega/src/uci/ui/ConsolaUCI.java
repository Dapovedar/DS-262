package uci.ui;

import java.io.Console;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import uci.modelo.Cama;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.modelo.Validador;
import uci.servicio.Aplicacion;
import uci.servicio.ResultadoAsignacion;
import uci.servicio.SistemaUCI;

/** Primera version: la misma aplicacion en consola, con login de paciente y funcionario. */
public class ConsolaUCI {

    private final SistemaUCI sistema;
    private final Aplicacion app;
    private final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

    public ConsolaUCI(Aplicacion app) {
        this.app = app;
        this.sistema = app.sistema;
    }

    public void ejecutar() {
        while (true) {
            System.out.println();
            System.out.println("=== " + Estilo.HOSPITAL.toUpperCase() + " ===");
            System.out.println("    " + Estilo.UNIDAD);
            System.out.println("1. Ingresar como paciente");
            System.out.println("2. Ingresar como funcionario");
            System.out.println("0. Salir");
            switch (leer("Opción: ")) {
                case "1": loginPaciente(); break;
                case "2": loginFuncionario(); break;
                case "0": System.out.println("Hasta luego."); return;
                default: System.out.println("  Opción no válida.");
            }
        }
    }

    // ------------------------------------------------------------ login

    private void loginPaciente() {
        String id = leerId("ID del paciente: ");
        try {
            Paciente p = app.autenticacion.ingresarPaciente(sistema, id);
            System.out.println("\nBienvenido(a), " + p.getNombre());
            System.out.println("  ID:           " + p.getId());
            System.out.println("  Diagnóstico:  " + p.getDiagnostico());
            System.out.println("  Índice:       " + p.getIndiceGravedad() + " (" + p.nivelUrgencia() + ")");
            System.out.println("  Posición:     #" + sistema.posicion(id) + " de " + sistema.cantidad());
            if (p.tieneCama()) {
                System.out.println("  ESTADO:       CAMA UCI ASIGNADA -> " + Cama.descripcion(p.getCama()));
            } else {
                System.out.println("  ESTADO:       EN LISTA DE ESPERA (sin cama asignada)");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void loginFuncionario() {
        String id = leerId("ID del funcionario: ");
        String clave = leerClave("Clave: ");
        try {
            Funcionario f = app.autenticacion.ingresarFuncionario(id, clave);
            System.out.println("\nSesión iniciada: " + f.getNombre() + " (" + f.getCargo() + ")");
            menuFuncionario();
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ menu del funcionario

    private void menuFuncionario() {
        while (true) {
            System.out.println();
            System.out.println("--- MENÚ DEL FUNCIONARIO ---");
            System.out.println("1. Registrar paciente");
            System.out.println("2. Consultar paciente por ID");
            System.out.println("3. Actualizar índice de gravedad");
            System.out.println("4. Dar de alta (eliminar)");
            System.out.println("5. Definir camas disponibles");
            System.out.println("6. Ejecutar asignación de camas");
            System.out.println("7. Lista de prioridad completa");
            System.out.println("8. Pacientes con cama / en espera");
            System.out.println("0. Cerrar sesión");
            try {
                switch (leer("Opción: ")) {
                    case "1": registrar(); break;
                    case "2": consultar(); break;
                    case "3": actualizar(); break;
                    case "4": alta(); break;
                    case "5": camas(); break;
                    case "6": asignar(); break;
                    case "7": imprimir("LISTA DE PRIORIDAD", sistema.listarOrdenado()); break;
                    case "8":
                        imprimir("CON CAMA", sistema.conCama());
                        imprimir("EN LISTA DE ESPERA", sistema.enEspera());
                        break;
                    case "0": System.out.println("Sesión cerrada."); return;
                    default: System.out.println("  Opción no válida.");
                }
            } catch (IllegalArgumentException | UncheckedIOException e) {
                System.out.println("  Error: " + e.getMessage());
            }
            if (sistema.isCambiosPendientes()) {
                System.out.println("  (Hay cambios sin asignar: use la opción 6)");
            }
        }
    }

    private void registrar() {
        String id = leerId("ID: ");
        if (sistema.existe(id)) {
            System.out.println("  Error: el ID " + id + " ya existe en la base de datos de pacientes.");
            return;
        }
        String nombre = leerTexto("Nombre completo: ", true);
        String diagnostico = leerTexto("Diagnóstico: ", false);
        int indice = leerEntero("Índice de gravedad (0-100): ", Validador.INDICE_MIN, Validador.INDICE_MAX);
        Paciente p = sistema.registrar(id, nombre, diagnostico, indice);
        System.out.println("  Registrado y guardado en la base de datos: " + p);
    }

    private void consultar() {
        String id = leerId("ID a consultar: ");
        Paciente p = sistema.buscar(id);
        if (p == null) {
            System.out.println("  El ID " + id + " NO existe en la base de datos de pacientes.");
            return;
        }
        System.out.println("  El ID " + id + " existe en la base de datos.");
        System.out.println("  " + p + " | " + p.getDiagnostico());
        System.out.println("  Posición #" + sistema.posicion(id) + " | "
                + (p.tieneCama() ? Cama.descripcion(p.getCama()) : "En lista de espera"));
    }

    private void actualizar() {
        String id = leerId("ID del paciente: ");
        if (!sistema.existe(id)) {
            System.out.println("  El ID " + id + " NO existe en la base de datos de pacientes.");
            return;
        }
        int antes = sistema.posicion(id);
        int nuevo = leerEntero("Nuevo índice (0-100): ", Validador.INDICE_MIN, Validador.INDICE_MAX);
        sistema.modificarIndice(id, nuevo);
        System.out.println("  Índice actualizado. Posición: #" + antes + " -> #" + sistema.posicion(id));
    }

    private void alta() {
        String id = leerId("ID del paciente: ");
        Paciente p = sistema.buscar(id);
        if (p == null) {
            System.out.println("  El ID " + id + " NO existe en la base de datos de pacientes.");
            return;
        }
        if (leer("¿Confirma el alta de " + p.getNombre() + "? (s/n): ").equalsIgnoreCase("s")) {
            sistema.darAlta(id);
            System.out.println("  Alta registrada. Paciente eliminado de la base de datos.");
        }
    }

    private void camas() {
        sistema.definirCamas(leerEntero("Camas disponibles (1-" + Cama.MAX + "): ", 1, Cama.MAX));
        System.out.println("  Camas definidas: " + sistema.getCamas());
    }

    private void asignar() {
        ResultadoAsignacion r = sistema.asignar();
        System.out.println("  Camas disponibles: " + sistema.getCamas());
        imprimir("CON CAMA", r.conCama());
        imprimir("EN LISTA DE ESPERA", r.enEspera());
    }

    private void imprimir(String titulo, List<Paciente> lista) {
        System.out.println("-- " + titulo + " (" + lista.size() + ") --");
        if (lista.isEmpty()) {
            System.out.println("   (vacío)");
            return;
        }
        int i = 1;
        for (Paciente p : lista) {
            System.out.printf("%3d. %-11s %-34s índice %3d  %s%n", i++, p.getId(), p.getNombre(),
                    p.getIndiceGravedad(), p.tieneCama() ? Cama.codigo(p.getCama()) : "En espera");
        }
    }

    // ------------------------------------------------------------ lectura validada

    private String leer(String mensaje) {
        System.out.print(mensaje);
        if (!in.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return in.nextLine().trim();
    }

    private String leerClave(String mensaje) {
        Console c = System.console();
        if (c != null) {
            char[] clave = c.readPassword(mensaje);
            return clave == null ? "" : new String(clave);
        }
        return leer(mensaje);
    }

    /** Repite la pregunta hasta que el ID sea solo numeros (5 a 12 digitos). */
    private String leerId(String mensaje) {
        while (true) {
            String s = leer(mensaje);
            String error = Validador.validarId(s);
            if (error == null) {
                return s;
            }
            System.out.println("  Error: " + error);
        }
    }

    /** Repite la pregunta hasta que el texto sean solo letras y espacios. */
    private String leerTexto(String mensaje, boolean esNombre) {
        while (true) {
            String s = leer(mensaje);
            String error = esNombre ? Validador.validarNombre(s) : Validador.validarDiagnostico(s);
            if (error == null) {
                return s;
            }
            System.out.println("  Error: " + error);
        }
    }

    /** Repite la pregunta hasta que se escriba un numero entero dentro del rango. */
    private int leerEntero(String mensaje, int min, int max) {
        while (true) {
            String s = leer(mensaje);
            if (s.isEmpty() || !Validador.soloDigitos(s) || s.length() > 9) {
                System.out.println("  Error: solo se permiten números enteros.");
                continue;
            }
            int v = Integer.parseInt(s);
            if (v < min || v > max) {
                System.out.println("  Error: el valor debe estar entre " + min + " y " + max + ".");
                continue;
            }
            return v;
        }
    }
}
