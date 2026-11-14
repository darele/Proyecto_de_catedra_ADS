package sv.edu.udb.rbo;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class SeguridadUtil {

    private static String cargarSalt() {
        try (InputStream input = SeguridadUtil.class.getClassLoader().getResourceAsStream("secret.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String salt = prop.getProperty("app.salt");
                if (salt != null && !salt.isEmpty()) {
                    return salt.trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer secret.properties: " + e.getMessage());
        }
        String envSalt = System.getenv("ADS_SALT");
        if (envSalt != null && !envSalt.isEmpty()) {
            return envSalt;
        }
        throw new RuntimeException("No se encontró la SALT en secret.properties.");
    }

    public static String encriptarSHA256(String password) {
        if (password == null) return null;
        try {
            String textoACifrar = password + cargarSalt();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(textoACifrar.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar en SHA-256", e);
        }
    }
}