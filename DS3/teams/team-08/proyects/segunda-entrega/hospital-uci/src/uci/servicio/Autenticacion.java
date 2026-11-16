package uci.servicio;

import java.util.HashMap;
import java.util.List;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.modelo.Validador;

/**
 * Inicio de sesion.
 *  - Paciente: solo su ID (debe existir en la base de datos de pacientes).
 *  - Funcionario: ID y clave (la clave se compara contra su hash).
 * Los funcionarios se guardan en un HashMap (ID -> Funcionario) para validar el ID en O(1).
 * Tras 3 claves incorrectas el ID queda bloqueado durante la sesion del programa.
 */
public class Autenticacion {

    public static final int MAX_INTENTOS = 3;

    private final HashMap<String, Funcionario> funcionarios = new HashMap<>();
    private final HashMap<String, Integer> intentosFallidos = new HashMap<>();

    public Autenticacion(List<Funcionario> lista) {
        for (Funcionario f : lista) {
            funcionarios.put(f.getId(), f);
        }
    }

    public int cantidadFuncionarios() { return funcionarios.size(); }

    public boolean existeFuncionario(String id) {
        return id != null && funcionarios.containsKey(id.trim());
    }

    /** Valida ID y clave. Lanza IllegalArgumentException con el motivo si no se puede ingresar. */
    public Funcionario ingresarFuncionario(String id, String clave) {
        String error = Validador.validarId(id);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        id = id.trim();
        Funcionario f = funcionarios.get(id);
        if (f == null) {
            throw new IllegalArgumentException("El ID " + id + " no existe en la base de datos de funcionarios.");
        }
        int fallos = intentosFallidos.getOrDefault(id, 0);
        if (fallos >= MAX_INTENTOS) {
            throw new IllegalArgumentException("Acceso bloqueado por " + MAX_INTENTOS
                    + " intentos fallidos. Reinicie la aplicación.");
        }
        if (!Seguridad.verificar(clave, f.getClaveProtegida())) {
            intentosFallidos.put(id, fallos + 1);
            int restantes = MAX_INTENTOS - fallos - 1;
            throw new IllegalArgumentException("Clave incorrecta. Intentos restantes: " + restantes + ".");
        }
        intentosFallidos.remove(id);
        return f;
    }

    /** El paciente ingresa solo con su ID, que debe existir en la base de datos. */
    public Paciente ingresarPaciente(SistemaUCI sistema, String id) {
        String error = Validador.validarId(id);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        Paciente p = sistema.buscar(id);
        if (p == null) {
            throw new IllegalArgumentException("El ID " + id.trim() + " no existe en la base de datos de pacientes.");
        }
        return p;
    }
}
