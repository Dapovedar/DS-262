package uci.modelo;

/** Funcionario del hospital con acceso completo al sistema. La clave se guarda protegida (salt$hash). */
public class Funcionario {

    private final String id;
    private final String nombre;
    private final String cargo;
    private final String claveProtegida;

    public Funcionario(String id, String nombre, String cargo, String claveProtegida) {
        this.id = id;
        this.nombre = nombre;
        this.cargo = cargo;
        this.claveProtegida = claveProtegida;
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getCargo() { return cargo; }

    public String getClaveProtegida() { return claveProtegida; }
}
