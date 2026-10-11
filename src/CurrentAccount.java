import java.math.BigDecimal;

/** Current account: can be drawn down to zero. */
public class CurrentAccount extends Account {
    public CurrentAccount(String accountNumber, String owner, BigDecimal balance) {
        super(accountNumber, owner, balance);
    }

    @Override public String getType() { return "CURRENT"; }
    @Override protected BigDecimal minimumBalance() { return BigDecimal.ZERO.setScale(2); }
}
