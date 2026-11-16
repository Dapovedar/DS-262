package uci.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;

/**
 * "Base de datos" del sistema en archivos de texto dentro de la carpeta data/:
 *   pacientes.csv     id;nombre;diagnostico;indice;cama
 *   funcionarios.csv  id;nombre;cargo;clave(salt$hash)
 *   config.properties camas, pendientes, ultimaAsignacion
 * Se guarda de forma segura: se escribe un archivo temporal y luego reemplaza al original.
 */
public class RepositorioCSV {

    private static final String SEP = ";";
    private final Path carpeta;
    private int lineasOmitidas;

    public RepositorioCSV(Path carpeta) {
        this.carpeta = carpeta;
    }

    /** Cantidad de lineas invalidas ignoradas en la ultima carga. */
    public int getLineasOmitidas() { return lineasOmitidas; }

    public List<Paciente> cargarPacientes() throws IOException {
        lineasOmitidas = 0;
        List<Paciente> lista = new ArrayList<>();
        Path archivo = carpeta.resolve("pacientes.csv");
        if (!Files.exists(archivo)) {
            return lista;
        }
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) { // la linea 0 es el encabezado
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] c = linea.split(SEP, -1);
            try {
                lista.add(new Paciente(c[0], c[1], c[2], Integer.parseInt(c[3].trim()), Integer.parseInt(c[4].trim())));
            } catch (RuntimeException e) { // formato o datos invalidos
                lineasOmitidas++;
                System.err.println("Línea " + (i + 1) + " de pacientes.csv ignorada: " + e.getMessage());
            }
        }
        return lista;
    }

    public void guardarPacientes(List<Paciente> pacientes) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("id;nombre;diagnostico;indice;cama");
        for (Paciente p : pacientes) {
            lineas.add(String.join(SEP, p.getId(), p.getNombre(), p.getDiagnostico(),
                    String.valueOf(p.getIndiceGravedad()), String.valueOf(p.getCama())));
        }
        escribirAtomico(carpeta.resolve("pacientes.csv"), lineas);
    }

    public List<Funcionario> cargarFuncionarios() throws IOException {
        List<Funcionario> lista = new ArrayList<>();
        Path archivo = carpeta.resolve("funcionarios.csv");
        if (!Files.exists(archivo)) {
            return lista;
        }
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] c = linea.split(SEP, -1);
            if (c.length == 4) {
                lista.add(new Funcionario(c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim()));
            }
        }
        return lista;
    }

    public Properties cargarConfig() throws IOException {
        Properties p = new Properties();
        Path archivo = carpeta.resolve("config.properties");
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        }
        return p;
    }

    public void guardarConfig(Properties config) throws IOException {
        Files.createDirectories(carpeta);
        Path tmp = carpeta.resolve("config.properties.tmp");
        try (OutputStream out = Files.newOutputStream(tmp)) {
            config.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), "Configuracion UCI");
        }
        Files.move(tmp, carpeta.resolve("config.properties"), StandardCopyOption.REPLACE_EXISTING);
    }

    private void escribirAtomico(Path destino, List<String> lineas) throws IOException {
        Files.createDirectories(carpeta);
        Path tmp = destino.resolveSibling(destino.getFileName() + ".tmp");
        Files.write(tmp, lineas, StandardCharsets.UTF_8);
        Files.move(tmp, destino, StandardCopyOption.REPLACE_EXISTING);
    }
}
