import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Business logic: registration, login, accounts and transactions. */
public class BankService {
    private static final String USERS = "users.txt";
    private static final String ACCOUNTS = "accounts.txt";
    private static final String TRANSACTIONS = "transactions.txt";

    private final FileStorage storage;
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    public BankService(FileStorage storage) {
        this.storage = storage;
        storage.load(USERS, User::fromRecord).forEach(u -> users.put(u.getUsername(), u));
        storage.load(ACCOUNTS, Account::fromRecord).forEach(a -> accounts.put(a.getAccountNumber(), a));
        transactions.addAll(storage.load(TRANSACTIONS, Transaction::fromRecord));
    }

    // ---------- Authentication ----------
    public void register(String username, char[] password) {
        if (!InputValidator.isValidUsername(username))
            throw new IllegalArgumentException("Username must be 3-20 letters, digits or underscores.");
        if (!InputValidator.isStrongPassword(password))
            throw new IllegalArgumentException("Password needs 8+ chars with upper, lower, digit and symbol.");
        if (users.containsKey(username.toLowerCase()))
            throw new IllegalArgumentException("Username already taken.");
        String salt = PasswordUtil.generateSalt();
        users.put(username.toLowerCase(), new User(username.toLowerCase(), salt, PasswordUtil.hash(password, salt), 0));
        saveUsers();
    }

    /** Returns the user on success. Generic error messages avoid revealing which part was wrong. */
    public User login(String username, char[] password) {
        User u = username == null ? null : users.get(username.toLowerCase());
        if (u == null) throw new SecurityException("Invalid username or password.");
        if (u.isLocked()) throw new SecurityException("Account locked after too many failed attempts. Contact the bank.");
        if (!PasswordUtil.verify(password, u.getSalt(), u.getPasswordHash())) {
            u.recordFailedAttempt();
            saveUsers();
            throw new SecurityException("Invalid username or password.");
        }
        u.resetFailedAttempts();
        saveUsers();
        return u;
    }

    // ---------- Accounts ----------
    public Account createAccount(User owner, String type) {
        String number;
        do {
            number = String.format("ACC%07d", random.nextInt(10_000_000));
        } while (accounts.containsKey(number));
        Account acc = "SAVINGS".equals(type)
                ? new SavingsAccount(number, owner.getUsername(), BigDecimal.ZERO)
                : new CurrentAccount(number, owner.getUsername(), BigDecimal.ZERO);
        accounts.put(number, acc);
        saveAccounts();
        return acc;
    }

    public List<Account> getAccounts(User owner) {
        return accounts.values().stream()
                .filter(a -> a.getOwner().equals(owner.getUsername()))
                .collect(Collectors.toList());
    }

    /** Authorization check: a user can only touch their own accounts. */
    private Account requireOwned(User owner, String accountNumber) {
        Account a = accounts.get(accountNumber);
        if (a == null || !a.getOwner().equals(owner.getUsername()))
            throw new SecurityException("Account not found.");
        return a;
    }

    // ---------- Transactions ----------
    public synchronized Account deposit(User owner, String accountNumber, BigDecimal amount) {
        Account a = requireOwned(owner, accountNumber);
        a.deposit(amount);
        record(a, Transaction.Type.DEPOSIT, amount);
        return a;
    }

    public synchronized Account withdraw(User owner, String accountNumber, BigDecimal amount) {
        Account a = requireOwned(owner, accountNumber);
        a.withdraw(amount);
        record(a, Transaction.Type.WITHDRAWAL, amount);
        return a;
    }

    public List<Transaction> history(User owner, String accountNumber) {
        requireOwned(owner, accountNumber);
        return transactions.stream().filter(t -> t.getAccountNumber().equals(accountNumber)).collect(Collectors.toList());
    }

    public List<Transaction> history(User owner) {
        Set<String> ownedAccountNumbers = getAccounts(owner).stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toSet());
        return transactions.stream()
                .filter(t -> ownedAccountNumbers.contains(t.getAccountNumber()))
                .collect(Collectors.toList());
    }

    private void record(Account a, Transaction.Type type, BigDecimal amount) {
        transactions.add(new Transaction(a.getAccountNumber(), type, amount, a.getBalance(), LocalDateTime.now()));
        saveAccounts();
        storage.save(TRANSACTIONS, transactions.stream().map(Transaction::toRecord).collect(Collectors.toList()));
    }

    private void saveUsers() {
        storage.save(USERS, users.values().stream().map(User::toRecord).collect(Collectors.toList()));
    }

    private void saveAccounts() {
        storage.save(ACCOUNTS, accounts.values().stream().map(Account::toRecord).collect(Collectors.toList()));
    }
}
