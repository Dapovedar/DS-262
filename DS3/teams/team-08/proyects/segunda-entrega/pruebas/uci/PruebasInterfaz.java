package uci;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.servicio.Aplicacion;
import uci.ui.Navegador;
import uci.ui.PanelFuncionario;
import uci.ui.PanelLogin;

/** Pruebas de la interfaz sin pantalla (headless): filtros de entrada, login y operaciones del funcionario. */
public class PruebasInterfaz {

    private static int ok = 0;
    private static int mal = 0;

    private static void v(String n, boolean c) {
        if (c) ok++; else mal++;
        System.out.println((c ? "  OK     " : "  FALLA  ") + n);
    }

    private static <T extends Component> List<T> todos(Container c, Class<T> tipo) {
        List<T> r = new ArrayList<>();
        for (Component k : c.getComponents()) {
            if (tipo.isInstance(k)) r.add(tipo.cast(k));
            if (k instanceof Container) r.addAll(todos((Container) k, tipo));
        }
        return r;
    }

    private static List<AbstractButton> botones(Container c, String texto) {
        List<AbstractButton> r = new ArrayList<>();
        for (AbstractButton b : todos(c, AbstractButton.class)) if (texto.equals(b.getText())) r.add(b);
        return r;
    }

    private static String textos(Container c) {
        StringBuilder sb = new StringBuilder();
        for (JLabel l : todos(c, JLabel.class)) sb.append(l.getText()).append(" | ");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path copia = Files.createTempDirectory("uci-gui"); // se trabaja sobre una copia de data/
        for (String n : new String[] {"pacientes.csv", "funcionarios.csv", "config.properties"}) {
            Files.copy(Path.of("data").resolve(n), copia.resolve(n));
        }
        Aplicacion app = Aplicacion.cargar(copia);
        final String[] destino = {""};
        Navegador nav = new Navegador() {
            public void mostrarLogin() { destino[0] = "login"; }
            public void mostrarFuncionario(Funcionario f) { destino[0] = "func:" + f.getNombre(); }
            public void mostrarPaciente(Paciente p) { destino[0] = "pac:" + p.getNombre(); }
        };

        System.out.println("[Filtros de entrada y login]");
        PanelLogin login = new PanelLogin(nav, app.sistema, app.autenticacion);
        JTextField id = todos(login, JTextField.class).stream().filter(t -> !(t instanceof JPasswordField)).findFirst().get();
        id.setText("12ab34");
        v("campo ID rechaza letras", id.getText().isEmpty());
        id.setText("1234567890123456");
        v("campo ID rechaza más de 12 dígitos", id.getText().isEmpty());
        String idPac = app.sistema.listarOrdenado().get(0).getId();
        id.setText(idPac);
        v("campo ID acepta números", id.getText().equals(idPac));
        botones(login, "Ingresar").get(0).doClick();
        v("paciente existente entra solo con ID", destino[0].startsWith("pac:"));
        destino[0] = "";
        id.setText("55555555");
        botones(login, "Ingresar").get(0).doClick();
        v("paciente con ID inexistente no entra", destino[0].isEmpty());
        v("se informa que el ID no existe en la base de datos", textos(login).contains("no existe en la base de datos de pacientes"));
        botones(login, "Soy funcionario").get(0).doClick();
        JPasswordField clave = todos(login, JPasswordField.class).get(0);
        id.setText("1010101010");
        clave.setText("incorrecta");
        botones(login, "Ingresar").get(0).doClick();
        v("funcionario con clave incorrecta no entra", destino[0].isEmpty() && textos(login).contains("Clave incorrecta"));
        id.setText("1010101010");
        clave.setText("Uci#2026a");
        botones(login, "Ingresar").get(0).doClick();
        v("funcionario con ID y clave correctos entra", destino[0].startsWith("func:Laura"));

        System.out.println("[Panel del funcionario: registrar]");
        Funcionario f = app.autenticacion.ingresarFuncionario("1020202020", "Hun2026*b");
        PanelFuncionario pf = new PanelFuncionario(nav, app.sistema, f);
        botones(pf, "Registrar paciente").get(0).doClick(); // menú lateral
        List<JTextField> campos = todos(pf, JTextField.class);
        JTextField rId = campos.get(0), rNom = campos.get(1), rDiag = campos.get(2), rInd = campos.get(3);
        rNom.setText("Juan 123");
        v("campo nombre rechaza números", rNom.getText().isEmpty());
        rNom.setText("Juan Pérez");
        v("campo nombre acepta letras y tildes", rNom.getText().equals("Juan Pérez"));
        rInd.setText("150");
        v("campo índice rechaza valores mayores a 100", rInd.getText().isEmpty());
        rInd.setText("8x");
        v("campo índice rechaza letras", rInd.getText().isEmpty());
        rId.setText("3001002003");
        rDiag.setText("Choque séptico");
        rInd.setText("3");
        int antes = app.sistema.cantidad();
        List<AbstractButton> reg = botones(pf, "Registrar paciente");
        AbstractButton enviar = reg.get(reg.size() - 1); // el último es el del formulario
        enviar.doClick();
        v("registro correcto aumenta la base de datos", app.sistema.cantidad() == antes + 1 && app.sistema.existe("3001002003"));
        v("el paciente más grave queda en la posición 1", app.sistema.posicion("3001002003") == 1);
        rId.setText("3001002003");
        rNom.setText("Otra Persona");
        rDiag.setText("Neumonía grave");
        rInd.setText("50");
        enviar.doClick();
        v("ID repetido se rechaza con mensaje", app.sistema.cantidad() == antes + 1 && textos(pf).contains("ya existe en la base de datos"));
        v("hay aviso de cambios sin asignar", app.sistema.isCambiosPendientes());

        System.out.println("[Panel del funcionario: gestionar y asignar]");
        botones(pf, "Gestionar paciente").get(0).doClick();
        campos = todos(pf, JTextField.class);
        JTextField gId = campos.get(4), gInd = campos.get(5);
        gId.setText("3001002003");
        botones(pf, "Buscar en la base de datos").get(0).doClick();
        v("buscar muestra que el ID existe en la base de datos", textos(pf).contains("existe en la base de datos de pacientes"));
        gInd.setText("95");
        botones(pf, "Actualizar índice").get(0).doClick();
        v("actualizar índice reubica al paciente", app.sistema.buscar("3001002003").getIndiceGravedad() == 95
                && app.sistema.posicion("3001002003") > 40);
        gId.setText("99999999");
        botones(pf, "Buscar en la base de datos").get(0).doClick();
        v("buscar ID inexistente lo informa", textos(pf).contains("NO existe en la base de datos"));
        botones(pf, "Camas y asignación").get(0).doClick();
        botones(pf, "Ejecutar asignación de camas").get(0).doClick();
        v("ejecutar asignación quita los cambios pendientes", !app.sistema.isCambiosPendientes());
        v("12 pacientes con cama tras asignar", app.sistema.conCama().size() == 12);

        System.out.println("\n=== RESUMEN INTERFAZ: " + ok + " pruebas correctas, " + mal + " fallidas ===");
        if (mal > 0) System.exit(1);
    }
}
