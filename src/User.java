/** A registered user. Credentials are encapsulated and only the hash is kept. */
public class User {
    public static final int MAX_FAILED_ATTEMPTS = 3;

    private final String username;
    private final String salt;
    private final String passwordHash;
    private int failedAttempts;

    public User(String username, String salt, String passwordHash, int failedAttempts) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.failedAttempts = failedAttempts;
    }

    public String getUsername() { return username; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return failedAttempts >= MAX_FAILED_ATTEMPTS; }
    public void recordFailedAttempt() { failedAttempts++; }
    public void resetFailedAttempts() { failedAttempts = 0; }

    public String toRecord() {
        return String.join("|", username, salt, passwordHash, String.valueOf(failedAttempts));
    }

    public static User fromRecord(String line) {
        String[] p = line.split("\\|");
        if (p.length != 4) throw new IllegalArgumentException("Corrupt user record");
        return new User(p[0], p[1], p[2], Integer.parseInt(p[3]));
    }
}
