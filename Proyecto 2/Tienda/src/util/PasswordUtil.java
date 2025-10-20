package util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitario simple para hash de contraseñas usando PBKDF2.
 */
public final class PasswordUtil {
    private static final SecureRandom RAND = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {}

    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        RAND.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        return String.format("%s:%s", Base64.getEncoder().encodeToString(salt), Base64.getEncoder().encodeToString(hash));
    }

    public static boolean verifyPassword(String password, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hash = Base64.getDecoder().decode(parts[1]);
        byte[] calc = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        if (calc.length != hash.length) return false;
        int diff = 0;
        for (int i = 0; i < calc.length; i++) diff |= calc[i] ^ hash[i];
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
