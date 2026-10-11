import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Central input validation: whitelist rules for every value entered by the user. */
public final class InputValidator {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern AMOUNT = Pattern.compile("^\\d{1,9}(\\.\\d{1,2})?$");
    public static final BigDecimal MAX_TRANSACTION = new BigDecimal("1000000");

    private InputValidator() {}

    public static boolean isValidUsername(String s) {
        return s != null && USERNAME.matcher(s).matches();
    }

    /** At least 8 chars with upper, lower, digit and symbol. */
    public static boolean isStrongPassword(char[] p) {
        if (p == null || p.length < 8 || p.length > 64) return false;
        boolean up = false, low = false, dig = false, sym = false;
        for (char c : p) {
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
            else if (Character.isDigit(c)) dig = true;
            else if (c == '|' || Character.isWhitespace(c)) return false;
            else sym = true;
        }
        return up && low && dig && sym;
    }

    /** Parses a positive money amount, or throws IllegalArgumentException. */
    public static BigDecimal parseAmount(String s) {
        if (s == null || !AMOUNT.matcher(s.trim()).matches())
            throw new IllegalArgumentException("Enter a valid amount (e.g. 150 or 150.50).");
        BigDecimal v = new BigDecimal(s.trim());
        if (v.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        if (v.compareTo(MAX_TRANSACTION) > 0)
            throw new IllegalArgumentException("Amount exceeds the limit of " + MAX_TRANSACTION + ".");
        return v.setScale(2);
    }
}
