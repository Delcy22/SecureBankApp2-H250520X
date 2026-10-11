import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/** Console user interface for the Secure Banking Application. */
public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final Console CONSOLE = System.console();

    public static void main(String[] args) throws IOException {
        BankService bank = new BankService(new FileStorage(resolveDataDirectory().toString()));
        System.out.println("=== Secure Banking Application ===");
        while (true) {
            System.out.println("\n1) Register  2) Login  3) Exit");
            switch (prompt("Choose: ")) {
                case "1" -> register(bank);
                case "2" -> {
                    User u = login(bank);
                    if (u != null) userMenu(bank, u);
                }
                case "3" -> { System.out.println("Goodbye."); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static Path resolveDataDirectory() throws IOException {
        Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
        if (Files.isDirectory(workingDirectory.resolve("src"))) {
            return workingDirectory.resolve("data");
        }

        try (Stream<Path> children = Files.list(workingDirectory)) {
            List<Path> projectDirectories = children
                    .filter(Files::isDirectory)
                    .filter(path -> Files.isDirectory(path.resolve("src"))
                            && Files.isDirectory(path.resolve("data")))
                    .toList();
            if (projectDirectories.size() == 1) {
                return projectDirectories.get(0).resolve("data");
            }
            if (projectDirectories.size() > 1) {
                throw new IllegalStateException("More than one project folder found; run the app from its project folder.");
            }
        }

        return workingDirectory.resolve("data");
    }

    private static void register(BankService bank) {
        String name = prompt("Username: ");
        char[] pw = readPassword("Password: ");
        char[] confirm = readPassword("Confirm password: ");
        try {
            if (!java.util.Arrays.equals(pw, confirm)) throw new IllegalArgumentException("Passwords do not match.");
            bank.register(name, pw);
            System.out.println("Registration successful. You can now log in.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            PasswordUtil.wipe(pw);
            PasswordUtil.wipe(confirm);
        }
    }

    private static User login(BankService bank) {
        String name = prompt("Username: ");
        char[] pw = readPassword("Password: ");
        try {
            User u = bank.login(name, pw);
            System.out.println("Welcome, " + u.getUsername() + "!");
            return u;
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        } finally {
            PasswordUtil.wipe(pw);
        }
    }

    private static void userMenu(BankService bank, User user) {
        while (true) {
            System.out.println("\n1) Create account  2) View balances  3) Deposit  4) Withdraw  5) Transaction history  6) Logout");
            try {
                switch (prompt("Choose: ")) {
                    case "1" -> {
                        String t = prompt("Type (1=Savings, 2=Current): ");
                        if (!t.equals("1") && !t.equals("2")) { System.out.println("Invalid type."); break; }
                        Account a = bank.createAccount(user, t.equals("1") ? "SAVINGS" : "CURRENT");
                        System.out.println("Created " + a);
                    }
                    case "2" -> {
                        List<Account> list = bank.getAccounts(user);
                        if (list.isEmpty()) System.out.println("You have no accounts yet.");
                        list.forEach(System.out::println);
                    }
                    case "3" -> {
                        String acc = prompt("Account number: ");
                        Account a = bank.deposit(user, acc, InputValidator.parseAmount(prompt("Amount: ")));
                        System.out.println("Deposit successful. New balance: " + a.getBalance());
                    }
                    case "4" -> {
                        String acc = prompt("Account number: ");
                        Account a = bank.withdraw(user, acc, InputValidator.parseAmount(prompt("Amount: ")));
                        System.out.println("Withdrawal successful. New balance: " + a.getBalance());
                    }
                    case "5" -> {
                        List<Transaction> h = bank.history(user);
                        if (h.isEmpty()) System.out.println("No transactions yet.");
                        h.forEach(transaction -> System.out.println(
                                transaction.getAccountNumber() + "  " + transaction));
                    }
                    case "6" -> { System.out.println("Logged out."); return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static String prompt(String msg) {
        System.out.print(msg);
        if (!IN.hasNextLine()) System.exit(0);
        return IN.nextLine().trim();
    }

    /** Hides typed password when running in a real terminal. */
    private static char[] readPassword(String msg) {
        if (CONSOLE != null) {
            char[] p = CONSOLE.readPassword(msg);
            return p == null ? new char[0] : p;
        }
        return prompt(msg).toCharArray();
    }
}
