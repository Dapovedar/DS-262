package uci.servicio;

import java.util.List;
import uci.modelo.Paciente;

/**
 * Resultado de una asignacion de camas.
 * @param conCama  pacientes con cama, de mayor a menor urgencia
 * @param enEspera pacientes en lista de espera, de mayor a menor urgencia
 */
public record ResultadoAsignacion(List<Paciente> conCama, List<Paciente> enEspera) { }
