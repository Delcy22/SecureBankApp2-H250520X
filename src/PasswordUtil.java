import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** Secure password hashing using salted PBKDF2 (passwords are never stored in plain text). */
public final class PasswordUtil {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(char[] password, String saltB64) {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(saltB64), ITERATIONS, KEY_LENGTH);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Constant-time comparison to prevent timing attacks. */
    public static boolean verify(char[] password, String salt, String expectedHash) {
        String actual = hash(password, salt);
        return MessageDigest.isEqual(actual.getBytes(), expectedHash.getBytes());
    }

    public static void wipe(char[] data) {
        if (data != null) Arrays.fill(data, '\0');
    }
}
