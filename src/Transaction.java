import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Immutable record of a deposit or withdrawal. */
public final class Transaction {
    public enum Type { DEPOSIT, WITHDRAWAL }
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String accountNumber;
    private final Type type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime timestamp;

    public Transaction(String accountNumber, Type type, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime timestamp) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
    }

    public String getAccountNumber() { return accountNumber; }

    public String toRecord() {
        return String.join("|", accountNumber, type.name(), amount.toPlainString(),
                balanceAfter.toPlainString(), timestamp.format(FMT));
    }

    public static Transaction fromRecord(String line) {
        String[] p = line.split("\\|");
        if (p.length != 5) throw new IllegalArgumentException("Corrupt transaction record");
        return new Transaction(p[0], Type.valueOf(p[1]), new BigDecimal(p[2]),
                new BigDecimal(p[3]), LocalDateTime.parse(p[4], FMT));
    }

    @Override
    public String toString() {
        return String.format("%s  %-10s %12s   balance: %s", timestamp.format(FMT), type, amount, balanceAfter);
    }
}
