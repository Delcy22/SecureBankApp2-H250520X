import java.math.BigDecimal;

/** Savings account: must keep a minimum balance of 50.00. */
public class SavingsAccount extends Account {
    private static final BigDecimal MIN = new BigDecimal("50.00");

    public SavingsAccount(String accountNumber, String owner, BigDecimal balance) {
        super(accountNumber, owner, balance);
    }

    @Override public String getType() { return "SAVINGS"; }
    @Override protected BigDecimal minimumBalance() { return MIN; }
}
