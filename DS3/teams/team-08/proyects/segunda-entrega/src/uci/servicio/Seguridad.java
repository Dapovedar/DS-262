package uci.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** Proteccion de claves: se guarda "salt$SHA-256(salt+clave)", nunca la clave en texto plano. */
public final class Seguridad {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Seguridad() { }

    public static String proteger(String clave) {
        byte[] bytes = new byte[8];
        ALEATORIO.nextBytes(bytes);
        String salt = hex(bytes);
        return salt + "$" + hash(clave, salt);
    }

    public static boolean verificar(String clave, String protegida) {
        if (clave == null || protegida == null) {
            return false;
        }
        int i = protegida.indexOf('$');
        if (i < 0) {
            return false;
        }
        String salt = protegida.substring(0, i);
        String esperado = protegida.substring(i + 1);
        return MessageDigest.isEqual(
                hash(clave, salt).getBytes(StandardCharsets.UTF_8),
                esperado.getBytes(StandardCharsets.UTF_8));
    }

    private static String hash(String clave, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest((salt + clave).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
