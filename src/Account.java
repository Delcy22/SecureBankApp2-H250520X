import java.math.BigDecimal;

/** Base bank account. Balance can only change through validated deposit/withdraw methods. */
public abstract class Account {
    private final String accountNumber;
    private final String owner;
    private BigDecimal balance;

    protected Account(String accountNumber, String owner, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance.setScale(2);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }
    public abstract String getType();

    /** Minimum balance that must remain after a withdrawal (polymorphic rule). */
    protected abstract BigDecimal minimumBalance();

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Invalid deposit amount.");
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Invalid withdrawal amount.");
        if (balance.subtract(amount).compareTo(minimumBalance()) < 0)
            throw new IllegalStateException("Insufficient funds. Minimum balance for " + getType()
                    + " account is " + minimumBalance() + ".");
        balance = balance.subtract(amount);
    }

    public String toRecord() {
        return String.join("|", accountNumber, owner, getType(), balance.toPlainString());
    }

    public static Account fromRecord(String line) {
        String[] p = line.split("\\|");
        if (p.length != 4) throw new IllegalArgumentException("Corrupt account record");
        BigDecimal bal = new BigDecimal(p[3]);
        return switch (p[2]) {
            case "SAVINGS" -> new SavingsAccount(p[0], p[1], bal);
            case "CURRENT" -> new CurrentAccount(p[0], p[1], bal);
            default -> throw new IllegalArgumentException("Unknown account type");
        };
    }

    @Override
    public String toString() {
        return String.format("%s [%s]  balance: %s", accountNumber, getType(), balance);
    }
}
