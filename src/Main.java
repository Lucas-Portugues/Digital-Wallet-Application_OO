import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    //DECLARE VARIABLES
    static Scanner scanner = new Scanner(System.in);
    static boolean isRunning = true;
    static int choice;

    //User banking details
    static BankAccount ac1 = new BankAccount("Banco do Brasil", "123456-7", 1500.00, 500.00);
    static BankAccount ac2 = new BankAccount("Caixa Econômica", "987654-3", 980.00, 250.00);

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
            System.out.println("5. Changed bank account PIN");
            System.out.println("6. Rewards and Points");
            System.out.println("7. Exit");
            System.out.println("***************");

            //GET AND PROCESS USERS CHOICE
            System.out.print("Enter your choice (1-7): ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> showChoice();
                case 2 -> card.securePayment();
                case 3 -> card.accountOperations();
                case 4 -> card.setPin();
                case 5 -> changeAccountPIN();
                case 6 -> card.rewardsMenu();
                case 7 -> isRunning = false;
                default -> System.out.println("INVALID CHOICE");
            }
        }

        System.out.println("***************************");
        System.out.println("Thank you! have a nice day!");
        System.out.println("***************************");

        //EXIT MESSAGE
        scanner.close();
    }

    static void changeAccountPIN() {
        System.out.println("Select account to change PIN:");
        if(card.linkedAccounts.isEmpty()){
            System.out.println("No linked accounts.");
            return;
        }

        for(int i = 0; i < card.linkedAccounts.size(); i++){
            System.out.println((i + 1) + " - " + card.linkedAccounts.get(i).accountNumber);
        }

        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        if(choice < 1 || choice > card.linkedAccounts.size()){
            System.out.println("INVALID CHOICE");
            return;
        }

        BankAccount acc = card.linkedAccounts.get(choice - 1);
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
                case 1 -> card.linkedAccount(ac1, ac2);
                case 2 -> card.listLinkedAccounts();
                case 3 -> card.unlinkedAccount(ac1, ac2);
                case 4 -> inSubMenu = false;
                default -> System.out.println("INVALID CHOICE");
            }
        }
    }

    //Bank account class
    static class BankAccount {
        private String bankName;
        private String accountNumber;
        private double balance;
        private boolean linked;
        private String pinHash;
        private int failedAttempts;
        private boolean blocked;
        private List<String> transactionHistory;
        private double creditLimit;
        private double creditUsed;

        public BankAccount(String bankName, String accountNumber, double balance, double creditLimit) {
            this.bankName = bankName;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.linked = false;
            this.pinHash = hashPIN("1234"); //Default pin
            this.failedAttempts = 0;
            this.blocked = false;
            this.transactionHistory = new ArrayList<>();
            this.creditLimit = creditLimit;
            this.creditUsed = 0.0;
        }

        public void deposit(double amount) {
            if(amount > 0){
                balance += amount;
                this.creditUsed = (this.balance < 0) ? -this.balance : 0;
            }
        }

        public boolean deductBalance(double amount){
            if (amount > getAvailableFunds()) {
                return false;
            }

            balance -= amount;
            this.creditUsed = (this.balance < 0) ? -this.balance : 0;

            addTransaction("Pagamento de R$" + String.format("%.2f", amount));
            return true;
        }

        public boolean checkPIN(String inputPIN){
            if(blocked) return false;
            return pinHash.equals(hashPIN(inputPIN));
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

        public boolean isBlocked(){
            return blocked;
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

        public void showInfo(){
            System.out.println("Bank: " + bankName);
            System.out.println("Account: " + accountNumber);
            System.out.printf("Balance: R$%.2f\n", balance);
            System.out.printf("Credit Limit: R$%.2f\n", creditLimit);
            System.out.printf("Available Credit: R$%.2f\n", creditLimit - creditUsed);
            System.out.println("Status: " + (linked ? "linked" : "unlinked"));
            System.out.println("Situation: " + (blocked ? "active" : "blocked"));
        }

        public void addTransaction(String transaction) {
            transactionHistory.add(transaction);
        }

        public double getBalance(){
            return balance;
        }

        public double getAvailableFunds() {
            return this.balance + (this.creditLimit - this.creditUsed);
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
    }

    //Digital wallet class
    static class DigitalWallet {
        private List<BankAccount> linkedAccounts = new ArrayList<>();
        private String pinCode;
        private int loyaltyPoints;

        public DigitalWallet() {
            this.loyaltyPoints = 0;
        }

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

        public void linkedAccount(BankAccount account, BankAccount account2) {
            if(!account.isLinked()){
                account.link();
                account2.link();
                linkedAccounts.add(account);
                linkedAccounts.add(account2);
            }else{
                System.out.println("Account already linked!");
            }
        }

        public void unlinkedAccount(BankAccount account, BankAccount account2) {
            if(linkedAccounts.contains(account) || linkedAccounts.contains(account2)) {
                account.unlink();
                account2.unlink();
                linkedAccounts.remove(account);
                linkedAccounts.remove(account2);
            }else{
                System.out.println("Account not linked!");
            }
        }

        public void listLinkedAccounts(){
            if(linkedAccounts.isEmpty()){
                System.out.println("No accounts linked!");
            }else{
                System.out.println("Accounts linked:");
                for(BankAccount account : linkedAccounts) {
                    account.showInfo();
                    System.out.println("------------");
                }
            }
        }

        public void securePayment(){
            if(linkedAccounts.isEmpty()){
                System.out.println("No linked accounts to make payment.");
                return;
            }

            System.out.println("Select account to use for payment:");
            for(int i = 0; i < linkedAccounts.size(); i++){
                System.out.println((i + 1) + " - " + linkedAccounts.get(i).accountNumber);
            }

            System.out.print("Enter choice: ");
            int accChoice = scanner.nextInt();

            if(accChoice < 1 || accChoice > linkedAccounts.size()){
                System.out.println("INVALID CHOICE");
                return;
            }

            BankAccount selectedAccount = linkedAccounts.get(accChoice - 1);

            int attempts = 3;
            boolean authenticated = false;

            while(attempts > 0 && !authenticated){
                System.out.print("Enter the 4-digit PIN: ");
                String inputPin = scanner.next();

                if(selectedAccount.checkPIN(inputPin)){
                    authenticated = true;
                    selectedAccount.resetFailedAttempts();
                }else{
                    selectedAccount.incrementFailedAttempts();
                    attempts--;
                    if(selectedAccount.isBlocked()){
                        System.out.println("Account blocked. Transaction canceled.");
                        return;
                    }
                    if(attempts > 0){
                        System.out.println("Incorrect PIN. Remaining attempts:" + attempts);
                    }
                }
            }

            if(!verifyPin()){
                System.out.println("Incorrect PIN. Payment canceled.");
                return;
            }

            System.out.print("Enter amount to pay: ");
            double amount = scanner.nextDouble();

            if(amount <= 0){
                System.out.println("Invalid payment amount.");
                return;
            }

            if(amount > selectedAccount.getAvailableFunds()){
                System.out.println("Insufficient funds (including credit limit).");
                return;
            }

            if(!selectedAccount.isLinked()){
                System.out.println("Selected account is not linked.");
                return;
            }

            System.out.print("Confirm payment of R$" + amount + "? (y/n): ");
            char confirm = scanner.next().toLowerCase().charAt(0);

            if(confirm == 'y'){
                if(selectedAccount.deductBalance(amount)){
                    System.out.println("Payment successful!");
                    System.out.printf("New balance: R$%.2f\n", selectedAccount.getBalance());

                    int pointsEarned = (int) (amount / 10);
                    if (pointsEarned > 0) {
                        this.loyaltyPoints += pointsEarned;
                        System.out.println("Congratulations! You earned " + pointsEarned + " loyalty points!");
                    }

                }else{
                    System.out.println("Payment failed. Insufficient funds.");
                }
            }else{
                System.out.println("Payment canceled!");
            }
        }

        public void rewardsMenu() {
            boolean inRewardsMenu = true;
            while (inRewardsMenu) {
                System.out.println("\n--- Rewards and Points Menu ---");
                System.out.println("1. Check points balance");
                System.out.println("2. Redeem points for cash");
                System.out.println("3. Back to main menu");
                System.out.println("-----------------------------");
                System.out.print("Enter your choice: ");
                int rewardsChoice = scanner.nextInt();

                switch (rewardsChoice) {
                    case 1:
                        System.out.println("\nYour current loyalty points balance is: " + this.loyaltyPoints);
                        break;
                    case 2:
                        redeemPoints();
                        break;
                    case 3:
                        inRewardsMenu = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }

        private void redeemPoints() {
            System.out.println("\n    Redeem Points");
            System.out.println("Your balance: " + this.loyaltyPoints + " points.");
            System.out.println("Rule: 10 points can be redeemed for R$1.00.");

            if (this.loyaltyPoints < 10) {
                System.out.println("You need at least 10 points to make a redemption.");
                return;
            }

            System.out.print("How many points do you want to redeem (must be a multiple of 10 and greater than 1)? ");
            int pointsToRedeem = scanner.nextInt();

            if (pointsToRedeem <= 0) {
                System.out.println("Invalid amount.");
                return;
            }
            if (pointsToRedeem > this.loyaltyPoints) {
                System.out.println("You do not have enough points.");
                return;
            }
            if (pointsToRedeem % 10 != 0) {
                System.out.println("The number of points must be a multiple of 10.");
                return;
            }

            double cashValue = pointsToRedeem / 10.0;

            System.out.println("Select the account to deposit the R$" + String.format("%.2f", cashValue) + ":");
            for (int i = 0; i < linkedAccounts.size(); i++) {
                System.out.println((i + 1) + " - " + linkedAccounts.get(i).accountNumber);
            }
            System.out.print("Enter your choice: ");
            int accChoice = scanner.nextInt();

            if (accChoice < 1 || accChoice > linkedAccounts.size()) {
                System.out.println("INVALID ACCOUNT CHOICE. Redemption canceled.");
                return;
            }

            BankAccount selectedAccount = linkedAccounts.get(accChoice - 1);

            this.loyaltyPoints -= pointsToRedeem;
            selectedAccount.deposit(cashValue);
            selectedAccount.addTransaction("Redeemed " + pointsToRedeem + " points for R$" + String.format("%.2f", cashValue));

            System.out.println("\nRedemption successful!");
            System.out.println("R$" + String.format("%.2f", cashValue) + " deposited into account " + selectedAccount.accountNumber);
            System.out.println("Your new points balance is: " + this.loyaltyPoints);
        }

        public void accountOperations() {
            if (linkedAccounts.isEmpty()) {
                System.out.println("No linked accounts.");
                return;
            }

            System.out.println("Select account:");
            for (int i = 0; i < linkedAccounts.size(); i++) {
                System.out.println((i + 1) + " - " + linkedAccounts.get(i).accountNumber);
            }

            int accChoice = scanner.nextInt();

            if (accChoice < 1 || accChoice > linkedAccounts.size()) {
                System.out.println("Invalid choice.");
                return;
            }

            BankAccount selectedAccount = linkedAccounts.get(accChoice - 1);

            System.out.println("1. Transfer to another account");
            System.out.println("2. Show balance and details");
            System.out.println("3. Show history");
            System.out.println("4. Back");

            int op = scanner.nextInt();

            switch (op) {
                case 1:
                    transferBetweenAccounts(selectedAccount);
                    break;
                case 2:
                    selectedAccount.showInfo();
                    break;
                case 3:
                    selectedAccount.printTransactionHistory();
                    break;
                default:
                    System.out.println("Returning...");
            }
        }

        private void transferBetweenAccounts(BankAccount origin){
            if(linkedAccounts.size() < 2){
                System.out.println("At least two linked accounts are required for a transfer.");
                return;
            }

            System.out.println("Enter the account number to tranfer to:");
            for(BankAccount acc : linkedAccounts){
                if(!acc.equals(origin)){
                    System.out.println("- " + acc.accountNumber);
                }
            }

            System.out.print("Enter target account number: ");
            String targetAccountNumber = scanner.next();

            BankAccount destination = null;
            for(BankAccount acc : linkedAccounts){
                if(acc.accountNumber.equals(targetAccountNumber) && acc != origin){
                    destination = acc;
                    break;
                }
            }

            if(destination == null){
                System.out.println("Target account not found or invalid.");
                return;
            }

            System.out.print("Enter amount to transfer: ");
            double amount = scanner.nextDouble();

            if(amount <= 0){
                System.out.println("Invalid amount.");
                return;
            }

            if(origin.getAvailableFunds() < amount){
                System.out.println("Insufficient funds (including credit limit).");
                return;
            }

            System.out.printf("Confirm transfer of R$%.2f from account %s to account %s? (y/n): ", amount, origin.accountNumber, destination.accountNumber);
            char confirm = scanner.next().toLowerCase().charAt(0);

            if(confirm == 'y'){
                if (origin.deductBalance(amount)) {
                    destination.deposit(amount);

                    origin.addTransaction("Transfer R$" + amount + " to account " + destination.accountNumber);
                    destination.addTransaction("Received R$" + amount + " from account " + origin.accountNumber);

                    System.out.println("Transfer completed successful!");
                } else {
                    System.out.println("Transfer failed.");
                }
            }else{
                System.out.println("Transfer canceled.");
            }
        }
    }
}