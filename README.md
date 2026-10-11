# Secure Banking Application

## Description
A console-based banking application written in Java that demonstrates Object-Oriented Programming
(encapsulation, inheritance, abstraction, polymorphism) together with secure coding practices.
Users can register, log in, open accounts, deposit, withdraw and view history, with all data saved to text files.

## Student Details
- **Name:** YOUR NAME HERE
- **Registration Number:** YOUR REG NUMBER HERE

## Features
- User registration and login (User Authentication)
- Passwords hashed with salted PBKDF2-HMAC-SHA256 (never stored in plain text)
- Strong password policy and whitelist input validation
- Account lockout after 3 failed login attempts
- Generic login error messages (no username enumeration)
- Create Savings (min. balance 50.00) or Current accounts
- View account balances
- Deposit and withdraw funds with overdraft protection
- Transaction history across all accounts owned by the logged-in user
- Users can only access their own accounts (authorization checks)
- Money handled with `BigDecimal` (no rounding errors)
- Data persistence to text files (`data/users.txt`, `data/accounts.txt`, `data/transactions.txt`) with safe atomic writes

## OOP Design
| Class | Role |
|-------|------|
| `Account` (abstract) | Encapsulated balance, deposit/withdraw rules |
| `SavingsAccount`, `CurrentAccount` | Inheritance + polymorphic minimum balance |
| `User`, `Transaction` | Domain models |
| `BankService` | Business logic |
| `FileStorage` | Persistence |
| `PasswordUtil`, `InputValidator` | Security helpers |
| `Main` | Console UI |

## How to Run
Requires Java 17 or newer.
```bash
javac -d out src/*.java
java -cp out Main
```
Data is stored in the project's `data` folder. If you launch the app from the parent
directory, it uses the data folder inside the single project folder it finds.

The `sample-data` folder contains fictional account and transaction records for
demonstration. Real local data files are excluded from GitHub.
