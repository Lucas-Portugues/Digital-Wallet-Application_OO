import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class novo {
    //DECLARE VARIABLES
    static Scanner scanner = new Scanner(System.in);
    static boolean isRunning = true;
    static int choice;
    static double balance = 0;

    //User banking details - usando polimorfismo
    static Account ac1 = new BankAccount("Banco do Brasil", "123456-7", 1500.00);
    static Account ac2 = new BankAccount("Caixa Econômica", "987654-3", 980.00);
    static Account cc1 = new CreditCard("Visa Premium", "4532-****-****-1234", 2000.00, 500.00);

    static DigitalWallet card = new DigitalWallet();

    public static void main(String[] args) {

        //DISPLAY MENU
        while(isRunning) {
            System.out.println("***************");
            System.out.println("Digital Wallet Application");
            System.out.println("***************");
            System.out.println("1. Show linked accounts");
            System.out.println("2. Secure payment");
            System.out.println("3. Manage account (transfer, balance, history)");
            System.out.println("4. Set/Change wallet PIN");
            System.out.println("5. Changed account PIN");
            System.out.println("6. Exit");
            System.out.println("***************");

            //GET AND PROCESS USERS CHOICE
            System.out.print("Enter your choice (1-6): ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> showChoice();
                case 2 -> card.securePayment();
                case 3 -> card.accountOperations();
                case 4 -> card.setPin();
                case 5 -> changeAccountPIN();
                case 6 -> isRunning = false;
                default -> System.out.println("INVALID CHOICE");
            }
        }

        System.out.println("***************************");
        System.out.println("Thank you! have a nice day!");
        System.out.println("***************************");

        scanner.close();
    }

    static void changeAccountPIN() {
        System.out.println("Select account to change PIN:");
        if(card.linkedAccounts.isEmpty()){
            System.out.println("No linked accounts.");
            return;
        }

        for(int i = 0; i < card.linkedAccounts.size(); i++){
            System.out.println((i + 1) + " - " + card.linkedAccounts.get(i).getAccountNumber());
        }

        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        if(choice < 1 || choice > card.linkedAccounts.size()){
            System.out.println("INVALID CHOICE");
            return;
        }

        Account acc = card.linkedAccounts.get(choice - 1);
        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.next();
        acc.changePIN(newPin);
    }

    static void showChoice() {
        boolean inSubMenu = true;

        while (inSubMenu) {
            System.out.println("************************");
            System.out.println("1 - Link accounts");
            System.out.println("2 - Show linked accounts");
            System.out.println("3 - Unlink account");
            System.out.println("4 - Return to main menu");
            System.out.println("************************");

            System.out.print("Enter your choice (1-4): ");
            int subChoice = scanner.nextInt();

            switch (subChoice) {
                case 1 -> card.linkedAccount(ac1, ac2, cc1);
                case 2 -> card.listLinkedAccounts();
                case 3 -> card.unlinkedAccount(ac1, ac2, cc1);
                case 4 -> inSubMenu = false;
                default -> System.out.println("INVALID CHOICE");
            }
        }
    }

    // INTERFACE para Processamento de Pagamentos - POLIMORFISMO
    interface PaymentProcessor {
        boolean processPayment(double amount);
        String getPaymentMethod();
    }

    // CLASSE ABSTRATA base para todas as contas - HERANÇA + CLASSE ABSTRATA
    static abstract class Account implements PaymentProcessor {
        protected String institutionName;
        protected String accountNumber;
        protected double balance;
        protected boolean linked;
        protected String pinHash;
        protected int failedAttempts;
        protected boolean blocked;
        protected List<String> transactionHistory;

        // Construtor da classe abstrata
        public Account(String institutionName, String accountNumber, double balance) {
            this.institutionName = institutionName;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.linked = false;
            this.pinHash = hashPIN("1234"); // Default pin
            this.failedAttempts = 0;
            this.blocked = false;
            this.transactionHistory = new ArrayList<>();
        }

        // MÉTODO ABSTRATO - deve ser implementado pelas classes filhas
        public abstract String getAccountType();
        public abstract double getAvailableBalance();

        // ENCAPSULAMENTO - métodos protegidos e privados
        protected String hashPIN(String pin) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(pin.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) sb.append(String.format("%02x", b));
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Erro ao criptografar o PIN", e);
            }
        }

        // Métodos comuns para todas as contas
        public boolean checkPIN(String inputPIN) {
            if (blocked) return false;
            return pinHash.equals(hashPIN(inputPIN));
        }

        public void deposit(double amount) {
            if (amount > 0) {
                balance += amount;
                addTransaction("Depósito de R$" + String.format("%.2f", amount));
            }
        }

        public void addTransaction(String transaction) {
            transactionHistory.add(transaction);
        }

        public double getBalance() {
            return balance;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void printTransactionHistory() {
            if (transactionHistory.isEmpty()) {
                System.out.println("No Transactions");
                return;
            }
            System.out.println("History of Transactions:");
            for (String tx : transactionHistory) {
                System.out.println("- " + tx);
            }
        }

        public boolean isBlocked() {
            return blocked;
        }

        public void incrementFailedAttempts() {
            failedAttempts++;
            if (failedAttempts >= 3) {
                blocked = true;
                System.out.println("Account blocked due to incorrect PIN attempts.");
            }
        }

        public void resetFailedAttempts() {
            failedAttempts = 0;
        }

        public void changePIN(String newPIN) {
            pinHash = hashPIN(newPIN);
            System.out.println("PIN changed successfully");
        }

        public void link() {
            this.linked = true;
            System.out.println("Account " + accountNumber + " successfully linked!");
        }

        public void unlink() {
            this.linked = false;
            System.out.println("Account " + accountNumber + " successfully unlinked!");
        }

        public boolean isLinked() {
            return linked;
        }

        // MÉTODO VIRTUAL que pode ser sobrescrito - POLIMORFISMO
        public void showInfo() {
            System.out.println("Institution: " + institutionName);
            System.out.println("Account: " + accountNumber);
            System.out.println("Type: " + getAccountType());
            System.out.printf("Available Balance: R$%.2f\n", getAvailableBalance());
            System.out.println("Status: " + (linked ? "linked" : "unlinked"));
            System.out.println("Situation: " + (blocked ? "blocked" : "active"));
        }
    }

    // HERANÇA - Classe filha BankAccount herda de Account
    static class BankAccount extends Account {

        public BankAccount(String bankName, String accountNumber, double balance) {
            super(bankName, accountNumber, balance); // chamada ao construtor da classe pai
        }

        // POLIMORFISMO - Override dos métodos abstratos
        @Override
        public String getAccountType() {
            return "Bank Account";
        }

        @Override
        public double getAvailableBalance() {
            return balance; // Conta bancária usa o saldo total
        }

        // POLIMORFISMO - Implementação da interface PaymentProcessor
        @Override
        public boolean processPayment(double amount) {
            if (amount <= balance && amount > 0) {
                balance -= amount;
                addTransaction("Pagamento via Conta Bancária de R$" + String.format("%.2f", amount));
                return true;
            }
            return false;
        }

        @Override
        public String getPaymentMethod() {
            return "Bank Transfer";
        }

        // POLIMORFISMO - Override do método showInfo para comportamento específico
        @Override
        public void showInfo() {
            super.showInfo(); // chama método da classe pai
            System.out.println("Bank Name: " + institutionName);
        }
    }

    // HERANÇA - Nova classe filha CreditCard herda de Account
    static class CreditCard extends Account {
        private double creditLimit;
        private double usedCredit;

        public CreditCard(String cardName, String cardNumber, double creditLimit, double usedCredit) {
            super(cardName, cardNumber, 0); // Cartão de crédito não tem saldo, mas sim limite
            this.creditLimit = creditLimit;
            this.usedCredit = usedCredit;
        }

        // POLIMORFISMO - Override dos métodos abstratos
        @Override
        public String getAccountType() {
            return "Credit Card";
        }

        @Override
        public double getAvailableBalance() {
            return creditLimit - usedCredit; // Saldo disponível é o limite menos o usado
        }

        // POLIMORFISMO - Implementação específica para cartão de crédito
        @Override
        public boolean processPayment(double amount) {
            if (amount <= getAvailableBalance() && amount > 0) {
                usedCredit += amount;
                addTransaction("Pagamento via Cartão de Crédito de R$" + String.format("%.2f", amount));
                return true;
            }
            return false;
        }

        @Override
        public String getPaymentMethod() {
            return "Credit Card";
        }

        // POLIMORFISMO - Override do método showInfo com comportamento específico
        @Override
        public void showInfo() {
            System.out.println("Institution: " + institutionName);
            System.out.println("Card: " + accountNumber);
            System.out.println("Type: " + getAccountType());
            System.out.printf("Credit Limit: R$%.2f\n", creditLimit);
            System.out.printf("Used Credit: R$%.2f\n", usedCredit);
            System.out.printf("Available Credit: R$%.2f\n", getAvailableBalance());
            System.out.println("Status: " + (linked ? "linked" : "unlinked"));
            System.out.println("Situation: " + (blocked ? "blocked" : "active"));
        }

        // Método específico para cartão de crédito
        public void payBill(double amount) {
            if (amount > 0 && amount <= usedCredit) {
                usedCredit -= amount;
                addTransaction("Pagamento de fatura R$" + String.format("%.2f", amount));
                System.out.println("Bill payment successful!");
            }
        }
    }

    //Digital wallet class - modificada para usar polimorfismo
    static class DigitalWallet {
        public List<Account> linkedAccounts = new ArrayList<>(); // Usando tipo da classe pai
        private String pinCode;

        public void setPin() {
            System.out.print("Set a 4-digit PIN: ");
            pinCode = scanner.next();
            System.out.println("PIN set successfully.");
        }

        private boolean verifyPin() {
            System.out.print("Enter your PIN: ");
            String inputPin = scanner.next();
            return pinCode != null && pinCode.equals(inputPin);
        }

        // POLIMORFISMO - método aceita qualquer tipo de Account
        public void linkedAccount(Account... accounts) {
            for (Account account : accounts) {
                if (!account.isLinked()) {
                    account.link();
                    linkedAccounts.add(account);
                } else {
                    System.out.println("Account " + account.getAccountNumber() + " already linked!");
                }
            }
        }

        public void unlinkedAccount(Account... accounts) {
            for (Account account : accounts) {
                if (linkedAccounts.contains(account)) {
                    account.unlink();
                    linkedAccounts.remove(account);
                } else {
                    System.out.println("Account " + account.getAccountNumber() + " not linked!");
                }
            }
        }

        public void listLinkedAccounts() {
            if (linkedAccounts.isEmpty()) {
                System.out.println("No accounts linked!");
            } else {
                System.out.println("Accounts linked:");
                for (Account account : linkedAccounts) {
                    account.showInfo(); // POLIMORFISMO - cada tipo mostra informações diferentes
                    System.out.println("------------");
                }
            }
        }

        // POLIMORFISMO - usa interface PaymentProcessor
        public void securePayment() {
            if (linkedAccounts.isEmpty()) {
                System.out.println("No linked accounts to make payment.");
                return;
            }

            System.out.println("Select account to use for payment:");
            for (int i = 0; i < linkedAccounts.size(); i++) {
                Account acc = linkedAccounts.get(i);
                System.out.println((i + 1) + " - " + acc.getAccountNumber() +
                        " (" + acc.getAccountType() + ") - Available: R$" +
                        String.format("%.2f", acc.getAvailableBalance()));
            }

            System.out.print("Enter choice: ");
            int accChoice = scanner.nextInt();

            if (accChoice < 1 || accChoice > linkedAccounts.size()) {
                System.out.println("INVALID CHOICE");
                return;
            }

            Account selectedAccount = linkedAccounts.get(accChoice - 1);

            int attempts = 3;
            boolean authenticated = false;

            while (attempts > 0 && !authenticated) {
                System.out.print("Enter the 4-digit PIN: ");
                String inputPin = scanner.next();

                if (selectedAccount.checkPIN(inputPin)) {
                    authenticated = true;
                    selectedAccount.resetFailedAttempts();
                } else {
                    selectedAccount.incrementFailedAttempts();
                    attempts--;
                    if (selectedAccount.isBlocked()) {
                        System.out.println("Account blocked. Transaction canceled.");
                        return;
                    }
                    if (attempts > 0) {
                        System.out.println("Incorrect PIN. Remaining attempts: " + attempts);
                    }
                }
            }

            if (!authenticated) {
                System.out.println("Authentication failed. Payment canceled.");
                return;
            }

            if (!verifyPin()) {
                System.out.println("Incorrect wallet PIN. Payment canceled.");
                return;
            }

            System.out.print("Enter amount to pay: ");
            double amount = scanner.nextDouble();

            if (amount <= 0) {
                System.out.println("Invalid payment amount.");
                return;
            }

            if (amount > selectedAccount.getAvailableBalance()) {
                System.out.println("Insufficient balance/credit");
                return;
            }

            System.out.printf("Confirm payment of R$%.2f via %s? (y/n): ",
                    amount, selectedAccount.getPaymentMethod());
            char confirm = scanner.next().toLowerCase().charAt(0);

            if (confirm == 'y') {
                // POLIMORFISMO - cada tipo de conta processa o pagamento diferente
                if (selectedAccount.processPayment(amount)) {
                    System.out.println("Payment successful via " + selectedAccount.getPaymentMethod() + "!");
                    System.out.printf("New available balance: R$%.2f\n", selectedAccount.getAvailableBalance());
                } else {
                    System.out.println("Payment failed.");
                }
            } else {
                System.out.println("Payment canceled!");
            }
        }

        public void accountOperations() {
            if (linkedAccounts.isEmpty()) {
                System.out.println("No linked accounts.");
                return;
            }

            System.out.println("Select account:");
            for (int i = 0; i < linkedAccounts.size(); i++) {
                System.out.println((i + 1) + " - " + linkedAccounts.get(i).getAccountNumber());
            }

            int accChoice = scanner.nextInt();

            if (accChoice < 1 || accChoice > linkedAccounts.size()) {
                System.out.println("Invalid choice.");
                return;
            }

            Account selectedAccount = linkedAccounts.get(accChoice - 1);

            System.out.println("1. Transfer to another account");
            System.out.println("2. Show balance");
            System.out.println("3. Show history");
            System.out.println("4. Back");

            int op = scanner.nextInt();

            switch (op) {
                case 1:
                    transferBetweenAccounts(selectedAccount);
                    break;
                case 2:
                    System.out.printf("Available Balance: R$%.2f\n", selectedAccount.getAvailableBalance());
                    break;
                case 3:
                    selectedAccount.printTransactionHistory();
                    break;
                default:
                    System.out.println("Returning...");
            }
        }

        private void transferBetweenAccounts(Account origin) {
            if (linkedAccounts.size() < 2) {
                System.out.println("At least two linked accounts are required for a transfer.");
                return;
            }

            System.out.println("Select destination account:");
            for (Account acc : linkedAccounts) {
                if (!acc.equals(origin)) {
                    System.out.println("- " + acc.getAccountNumber() + " (" + acc.getAccountType() + ")");
                }
            }

            System.out.print("Enter target account number: ");
            String targetAccountNumber = scanner.next();

            Account destination = null;
            for (Account acc : linkedAccounts) {
                if (acc.getAccountNumber().equals(targetAccountNumber) && acc != origin) {
                    destination = acc;
                    break;
                }
            }

            if (destination == null) {
                System.out.println("Target account not found or invalid.");
                return;
            }

            System.out.print("Enter amount to transfer: ");
            double amount = scanner.nextDouble();

            if (amount <= 0) {
                System.out.println("Invalid amount.");
                return;
            }

            if (origin.getAvailableBalance() < amount) {
                System.out.println("Insufficient funds.");
                return;
            }

            System.out.printf("Confirm transfer of R$%.2f from %s to %s? (y/n): ",
                    amount, origin.getAccountNumber(), destination.getAccountNumber());
            char confirm = scanner.next().toLowerCase().charAt(0);

            if (confirm == 'y') {
                // POLIMORFISMO - transferência funciona diferente para cada tipo
                if (origin.processPayment(amount)) {
                    destination.deposit(amount);

                    origin.addTransaction("Transfer R$" + amount + " to account " + destination.getAccountNumber());
                    destination.addTransaction("Received R$" + amount + " from account " + origin.getAccountNumber());

                    System.out.println("Transfer completed successfully!");
                } else {
                    System.out.println("Transfer failed.");
                }
            } else {
                System.out.println("Transfer canceled.");
            }
        }
    }
}
