package uci.ui;

import uci.modelo.Funcionario;
import uci.modelo.Paciente;

/** Permite a los paneles cambiar de pantalla sin conocer la ventana. */
public interface Navegador {
    void mostrarLogin();

    void mostrarFuncionario(Funcionario funcionario);

    void mostrarPaciente(Paciente paciente);
}
