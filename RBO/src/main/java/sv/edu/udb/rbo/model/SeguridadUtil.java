package sv.edu.udb.rbo.model;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Cifrado SHA-256 de contraseñas con SALT.
 *
 * <p>La SALT se lee de {@code secret.properties} (propiedad {@code app.salt}) en el
 * classpath, o de la variable de entorno {@code ADS_SALT}. secret.properties está en el
 * .gitignore; hay una plantilla en src/main/resources/secret.properties.example.</p>
 *
 * <p>Para crear un usuario a mano, ejecute el {@link #main(String[])} de esta clase
 * desde IntelliJ (clic derecho → Run) con la contraseña como argumento; imprime el hash
 * que va en la columna Empleado.password.</p>
 */
public final class SeguridadUtil {

    private static volatile String saltEnCache;

    private SeguridadUtil() {
    }

    private static String cargarSalt() {
        String salt = saltEnCache;
        if (salt != null) {
            return salt;
        }
        try (InputStream input = SeguridadUtil.class.getClassLoader().getResourceAsStream("secret.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String valor = prop.getProperty("app.salt");
                if (valor != null && !valor.isBlank()) {
                    saltEnCache = valor.trim();
                    return saltEnCache;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer secret.properties: " + e.getMessage());
        }
        String envSalt = System.getenv("ADS_SALT");
        if (envSalt != null && !envSalt.isBlank()) {
            saltEnCache = envSalt;
            return saltEnCache;
        }
        throw new IllegalStateException("No se encontró la SALT: cree src/main/resources/secret.properties"
                + " (copie secret.properties.example) o defina la variable de entorno ADS_SALT.");
    }

    public static String encriptarSHA256(String password) {
        if (password == null) {
            return null;
        }
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
            throw new IllegalStateException("Error al encriptar en SHA-256", e);
        }
    }

    /** Imprime el hash de cada contraseña recibida como argumento. */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: SeguridadUtil <contraseña> [<contraseña> ...]");
            return;
        }
        for (String password : args) {
            System.out.println(password + " -> " + encriptarSHA256(password));
        }
    }
}
