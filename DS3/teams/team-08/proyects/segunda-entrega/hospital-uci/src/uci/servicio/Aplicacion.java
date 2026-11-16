package uci.servicio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import uci.persistencia.RepositorioCSV;

/** Carga la "base de datos" (carpeta data/) y deja listos el sistema y la autenticacion. */
public class Aplicacion {

    public final SistemaUCI sistema;
    public final Autenticacion autenticacion;

    private Aplicacion(SistemaUCI sistema, Autenticacion autenticacion) {
        this.sistema = sistema;
        this.autenticacion = autenticacion;
    }

    public static Aplicacion cargar(Path carpetaDatos) throws IOException {
        if (!Files.isDirectory(carpetaDatos)) {
            throw new IOException("No se encontró la carpeta '" + carpetaDatos
                    + "'. Ejecute el programa desde la carpeta del proyecto.");
        }
        RepositorioCSV repo = new RepositorioCSV(carpetaDatos);
        Autenticacion auth = new Autenticacion(repo.cargarFuncionarios());
        if (auth.cantidadFuncionarios() == 0) {
            throw new IOException("La base de datos de funcionarios está vacía (data/funcionarios.csv).");
        }
        return new Aplicacion(new SistemaUCI(repo), auth);
    }
}
