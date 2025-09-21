import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

    public class newMain {

        static Scanner scanner = new Scanner(System.in);
        static boolean isRunning = true;
        static int choice;

        // Exemplo de contas
        static BankAccount ac1 = new BankAccount("Banco do Brasil", "123456-7", 1500.00);
        static BankAccount ac2 = new BankAccount("Caixa Econômica", "987654-3", 980.00);
        static CryptoAccount crypto = new CryptoAccount("1ABCD-XYZ-CRYPTO", 0.25);

        static DigitalWallet card = new DigitalWallet();

        public static void main(String[] args) {
            while(isRunning) {
                System.out.println("***************");
                System.out.println("Digital Wallet Application");
                System.out.println("***************");
                System.out.println("1. Show linked accounts");
                System.out.println("2. Secure payment");
                System.out.println("3. Manage account (transfer, balance, history)");
                System.out.println("4. Set/Change wallet PIN");
                System.out.println("5. Changed bank account PIN");
                System.out.println("6. Exit");
                System.out.println("***************");

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
            if(acc instanceof BankAccount bank){
                System.out.print("Enter new 4-digit PIN: ");
                String newPin = scanner.next();
                bank.changePIN(newPin);
            } else {
                System.out.println("This account type does not support PIN change.");
            }
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
                    case 1 -> {
                        card.linkedAccount(ac1);
                        card.linkedAccount(ac2);
                        card.linkedAccount(crypto);
                    }
                    case 2 -> card.listLinkedAccounts();
                    case 3 -> {
                        card.unlinkedAccount(ac1);
                        card.unlinkedAccount(ac2);
                        card.unlinkedAccount(crypto);
                    }
                    case 4 -> inSubMenu = false;
                    default -> System.out.println("INVALID CHOICE");
                }
            }
        }
    }

    // Classe abstrata
    abstract class Account {
        protected String accountNumber;
        protected double balance;

        public Account(String accountNumber, double balance) {
            this.accountNumber = accountNumber;
            this.balance = balance;
        }

        public abstract void showInfo();

        public void deposit(double amount) {
            if(amount > 0) balance += amount;
        }

        public double getBalance() {
            return balance;
        }

        public String getAccountNumber(){
            return accountNumber;
        }
    }

    // Conta bancária concreta
    class BankAccount extends Account {
        private String bankName;
        private boolean linked;
        private String pinHash;
        private int failedAttempts;
        private boolean blocked;
        private List<String> transactionHistory;

        public BankAccount(String bankName, String accountNumber, double balance) {
            super(accountNumber, balance);
            this.bankName = bankName;
            this.linked = false;
            this.pinHash = hashPIN("1234");
            this.failedAttempts = 0;
            this.blocked = false;
            this.transactionHistory = new ArrayList<>();
        }

        private String hashPIN(String pin){
            try{
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(pin.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) sb.append(String.format("%02x", b));
                return sb.toString();
            }catch (NoSuchAlgorithmException e){
                throw new RuntimeException("Erro ao criptografar o PIN", e);
            }
        }

        public boolean checkPIN(String inputPIN){
            if(blocked) return false;
            return pinHash.equals(hashPIN(inputPIN));
        }

        public boolean deductBalance(double amount){
            if(amount <= balance){
                balance -= amount;
                addTransaction("Pagamento de R$" + String.format("%.2f", amount));
                return true;
            }
            return false;
        }

        public void addTransaction(String transaction) {
            transactionHistory.add(transaction);
        }

        public void printTransactionHistory(){
            if(transactionHistory.isEmpty()){
                System.out.println("No Transactions");
                return;
            }
            System.out.println("History of Transactions:");
            for(String tx : transactionHistory){
                System.out.println("- " + tx);
            }
        }

        public void incrementFailedAttempts(){
            failedAttempts++;
            if(failedAttempts >= 3){
                blocked = true;
                System.out.println("Account blocked due to incorrect PIN attempts.");
            }
        }

        public void resetFailedAttempts(){
            failedAttempts = 0;
        }

        public void changePIN(String newPIN){
            pinHash = hashPIN(newPIN);
            System.out.println("PIN changed successfully");
        }

        public void link(){
            this.linked = true;
            System.out.println("Account " + accountNumber + " successfully linked!");
        }

        public void unlink(){
            this.linked = false;
            System.out.println("Account " + accountNumber + " successfully unlinked!");
        }

        public boolean isLinked(){
            return linked;
        }

        @Override
        public void showInfo(){
            System.out.println("Bank: " + bankName);
            System.out.println("Account: " + accountNumber);
            System.out.printf("Balance: R$%.2f\n", balance);
            System.out.println("Status: " + (linked ? "linked" : "unlinked"));
            System.out.println("Situation: " + (blocked ? "blocked" : "active"));
        }
    }

    // Outro tipo de conta (mostra polimorfismo)
    class CryptoAccount extends Account {
        private String walletAddress;

        public CryptoAccount(String walletAddress, double balance) {
            super("CRYPTO", balance);
            this.walletAddress = walletAddress;
        }

        @Override
        public void showInfo() {
            System.out.println("Crypto Wallet: " + walletAddress);
            System.out.printf("Balance: %.4f BTC\n", balance);
        }
    }

    // Carteira Digital
    class DigitalWallet {
        List<Account> linkedAccounts = new ArrayList<>();
        private String pinCode;
        private static Scanner scanner = new Scanner(System.in);

        public void setPin(){
            System.out.print("Set a 4-digit PIN: ");
            pinCode = scanner.next();
            System.out.println("PIN set sucessfully.");
        }

        private boolean verifyPin(){
            System.out.print("Enter your PIN: ");
            String inputPin = scanner.next();
            return pinCode != null && pinCode.equals(inputPin);
        }

        public void linkedAccount(Account account) {
            if(!linkedAccounts.contains(account)){
                linkedAccounts.add(account);
                if(account instanceof BankAccount bank){
                    bank.link();
                }
            }else{
                System.out.println("Account already linked!");
            }
        }

        public void unlinkedAccount(Account account) {
            if(linkedAccounts.contains(account)) {
                linkedAccounts.remove(account);
                if(account instanceof BankAccount bank){
                    bank.unlink();
                }
            }else{
                System.out.println("Account not linked!");
            }
        }

        public void listLinkedAccounts(){
            if(linkedAccounts.isEmpty()){
                System.out.println("No accounts linked!");
            }else{
                System.out.println("Accounts linked:");
                for(Account account : linkedAccounts) {
                    account.showInfo(); // Polimorfismo em ação
                    System.out.println("------------");
                }
            }
        }

        // Apenas exemplo simplificado de securePayment
        public void securePayment(){
            if(linkedAccounts.isEmpty()){
                System.out.println("No linked accounts to make payment.");
                return;
            }

            System.out.println("Select account to use for payment:");
            for(int i = 0; i < linkedAccounts.size(); i++){
                System.out.println((i + 1) + " - " + linkedAccounts.get(i).getAccountNumber());
            }

            System.out.print("Enter choice: ");
            int accChoice = scanner.nextInt();

            if(accChoice < 1 || accChoice > linkedAccounts.size()){
                System.out.println("INVALID CHOICE");
                return;
            }

            Account selectedAccount = linkedAccounts.get(accChoice - 1);

            if(selectedAccount instanceof BankAccount bank){
                if(!verifyPin()){
                    System.out.println("Incorrect PIN. Payment canceled.");
                    return;
                }

                System.out.print("Enter amount to pay: ");
                double amount = scanner.nextDouble();

                if(bank.deductBalance(amount)){
                    System.out.println("Payment successful!");
                } else {
                    System.out.println("Insufficient balance.");
                }
            } else {
                System.out.println("This account type does not support payments yet.");
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
            selectedAccount.showInfo();
        }
    }

