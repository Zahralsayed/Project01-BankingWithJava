package com.bank.user;
import com.bank.accounts.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class Customer extends User {
    protected Account savingAccount;
    protected Account checkingAccount;
    protected List<String> accountNumbers = new ArrayList<>();
    protected List<Account> accounts = new ArrayList<>();
    protected boolean firstLogin = true;
    protected boolean locked = false;

    List<Transaction> transactions;


    private static final String USERS_DIR = "src/data/users";

    public Customer(String id, String name, String password, Role role) {
        super(id, name, password, Role.Customer);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void removeAccount(String accountNumber) {
        accountNumbers.remove(accountNumber);
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setAccountNumbers(List<String> accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

    public List<String> getAccountNumbers() {
        return accountNumbers;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Account getSavingAccount() {
        if (savingAccount == null) {
            savingAccount = accounts.stream()
                    .filter(a -> a.getAccountType() == Account.AccountType.Saving)
                    .map(a -> (SavingAccount)a)
                    .findFirst()
                    .orElse(null);
        }
        return savingAccount;    }

    public void setSavingAccount(Account savingAccount) {
        this.savingAccount = savingAccount;
    }

    public Account getCheckingAccount() {
        if (checkingAccount == null) {
            checkingAccount = accounts.stream()
                    .filter(a -> a.getAccountType() == Account.AccountType.Checking)
                    .map(a -> (CheckingAccount) a)
                    .findFirst()
                    .orElse(null);
        }
        return checkingAccount;
    }

    public void setCheckingAccount(Account checkingAccount) {
        this.checkingAccount = checkingAccount;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    } //1

    private String safeName(String n) {
        return n.replaceAll("\s+", "_");
    }

public void saveCustomer(Customer c) {
    try {
        Path dir = Paths.get(USERS_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String fileName = "Customer-" + safeName(c.getName()) + "-" + c.getId() + ".txt";
        File f = new File(USERS_DIR, fileName);


        List<String> existingAccounts = new ArrayList<>();
        List<String> existingTransactions = new ArrayList<>();
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                boolean accountsSection = false;
                boolean transactionsSection = false;

                while ((line = br.readLine()) != null) {
                    if (line.equalsIgnoreCase("Accounts:")) {
                        accountsSection = true;
                        transactionsSection = false;
                        continue;
                    }
                    if (line.equalsIgnoreCase("Transactions:")) {
                        accountsSection = false;
                        transactionsSection = true;
                        continue;
                    }

                    if (accountsSection && !line.isEmpty()) {
                        existingAccounts.add(line);
                    }
                    if (transactionsSection && !line.isEmpty()) {
                        existingTransactions.add(line);
                    }
                }
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

            bw.write("ID:" + c.getId() + "\n");
            bw.write("Name:" + c.getName() + "\n");
            bw.write("Password:" + c.getPassword() + "\n");
            bw.write("Role:" + c.getRole() + "\n");
            bw.write("FirstLogin:" + c.isFirstLogin() + "\n");
            bw.write("\n");

            // Accounts
            bw.write("Accounts:\n");
            if (c.getAccounts() != null) {
                for (Account a : c.getAccounts()) {
                    String accLine = a.getAccountId() + "," +
                            a.getBalance() + "," +
                            a.getOverdraftCount() + "," +
                            a.getCard().getCardType() + "," +
                            a.getAccountType();



                    // replace old record with same accountId
                    String accId = a.getAccountId();
                    boolean updated = false;

                    for (int i = 0; i < existingAccounts.size(); i++) {
                        if (existingAccounts.get(i).startsWith(accId + ",")) {
                            existingAccounts.set(i, accLine);
                            updated = true;
                            break;
                        }
                    }

                    if (!updated) {
                        existingAccounts.add(accLine);
                    }


                }
            }
            for (String acc : existingAccounts) {
                bw.write(acc + "\n");
            }

            // Transactions
            bw.write("\nTransactions:\n");
            if (c.getAccounts() != null) {
                for (Account a  : c.getAccounts()) {
                    if (a.getTransactionHistory() != null) {
                        for (Transaction t : a.getTransactionHistory()) {
                            String txLine = t.getAccountId() + "," +
                                    t.getDateTime() + "," +
                                    t.getType() + "," +
                                    t.getAmount() + "," +
                                    t.getPreBalance()+ "," +
                                    t.getPostBalance() + "," +
                                    t.getDescription();
                            if (!existingTransactions.contains(txLine)) {
                                existingTransactions.add(txLine);
                            }
                        }
                    }
                }
            }
            for (String tx : existingTransactions) {
                bw.write(tx + "\n");
            }

            bw.flush();
        }


        updateDataFile(c);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void addAccountObject(Account a) {
        if (!accountNumbers.contains(a.getAccountId())) {
            accountNumbers.add(a.getAccountId());
        }
        boolean exists = accounts.stream()
                .anyMatch(acc -> acc.getAccountId().equals(a.getAccountId()));
        if (!exists) {
            accounts.add(a);
        }
    }

    private void updateDataFile(Customer c) {
        File file = new File(USERS_DIR + "/data.txt");
        List<String> lines = new ArrayList<>();
        boolean found = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 1 && parts[0].equals(c.getId())) {
                        line = String.join(",", c.getId(), c.getName(), c.getPassword(), String.valueOf(c.getRole()), "0","0");
                        found = true;
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!found) {
            lines.add(String.join(",",
                    c.getId(), c.getName(), c.getPassword(), String.valueOf(c.getRole()), "0","0"));
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateCustomerLineInDataFile(Customer updated) {
        File file = new File(USERS_DIR+"/data.txt");
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(updated.getId())) {
                    line = updated.getId() + "," +
                            updated.getName() + "," +
                            updated.getPassword() + "," +
                            updated.getRole();
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Customer loadCustomer(String id) {
        File folder = new File(USERS_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Users directory not found!");
            return null;
        }

        for (File f : folder.listFiles()) {
            if (f.getName().contains(id)) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {

                    String line;
                    String custId = null, name = null, password = null;
                    User.Role role = User.Role.Customer;
                    int failedAttempts = 0;
                    long lockUntil = 0;
                    boolean firstLogin = true;
                    List<Account> accounts = new ArrayList<>();
                    List<String> accountNumbers = new ArrayList<>();
                    boolean inAccountsSection = false;
                    boolean inTransactionsSection = false;

                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        if (line.equals("Accounts:")) {
                            inAccountsSection = true;
                            inTransactionsSection = false;
                            continue;
                        } else if (line.equals("Transactions:")) {
                            inAccountsSection = false;
                            inTransactionsSection = true;
                            continue;
                        }

                        if (inAccountsSection) {
                            String[] parts = line.split(",");


                            if (parts.length < 5 || parts[1].isEmpty() || parts[2].isEmpty()) {
                                continue;
                            }

                            if (parts.length >= 5) {
                                String accId = parts[0];
                                double balance = Double.parseDouble(parts[1]);
                                int overdraftCount = Integer.parseInt(parts[2]);
                                CardType card = new CardType(CardType.DebitCardType.valueOf(parts[3]));
                                String type = parts[4];

                                Account acc;
                                if (type.equals("Checking")) {
                                    acc = new CheckingAccount(accId, id, balance, card);
                                } else {
                                    acc = new SavingAccount(accId, id, balance, card);
                                }
                                acc.setOverdraftCount(overdraftCount);

                                accounts.add(acc);
                                accountNumbers.add(accId);
                            }
                            continue;


                        }

                        if (inTransactionsSection) {

                            String[] tx = line.split(",",7);
                            if (tx.length >= 6) {
                                String accId = tx[0];
                                LocalDateTime dateTime =  LocalDateTime.parse(tx[1]);
                                Transaction.TransactionType type = Transaction.TransactionType.valueOf(tx[2].trim());
                                double amount = Double.parseDouble(tx[3]);
                                double preBalance = Double.parseDouble(tx[4]);
                                double postBalance = Double.parseDouble(tx[5]);

                                String description = (tx.length == 7) ? tx[6].trim() : "";

                                Transaction t = new Transaction(accId,type,amount,preBalance,postBalance,dateTime,description);

                                for (Account acc : accounts) {
                                    if (acc.getAccountId().equals(accId)) {
                                        acc.addTransaction(t);
                                        break;
                                    }
                                }
                            }
                            continue;

                        }

                        if (line.startsWith("ID:")) custId = line.substring(3);
                        else if (line.startsWith("Name:")) name = line.substring(5);
                        else if (line.startsWith("Password:")) password = line.substring(9);
                        else if (line.startsWith("Role:")) role = User.Role.valueOf(line.substring(5));
                        else if (line.startsWith("FailedAttempts:")) failedAttempts = Integer.parseInt(line.substring(15));
                        else if (line.startsWith("LockUntil:")) lockUntil = Long.parseLong(line.substring(10));
                        else if (line.startsWith("FirstLogin:")) firstLogin = Boolean.parseBoolean(line.substring(11));
                    }

                    if (custId != null && name != null && password != null) {
                        Customer c = new Customer(custId, name, password, role);
                        c.setAccountNumbers(accountNumbers);
                        c.setAccounts(accounts);
                        c.setFailedAttempts(failedAttempts);
                        c.setLockTime(lockUntil);
                        c.setFirstLogin(firstLogin);
                        return c;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return null;

    }

    public Account getAccountByType(Account.AccountType type) {
        return accounts.stream()
                .filter(a -> a.getAccountType() == type)
                .findFirst()
                .orElse(null);
    }

    public void depositToAccount(Account.AccountType type, double amount, String description) throws Exception {
        Account a = getAccountByType(type);
        if (a == null) {
            throw new Exception(type + " account not found");
        }

        a.deposit(amount, description);
    }

    public void withdrawFromAccount(Account.AccountType type, double amount, String description) throws Exception {
        Account a = getAccountByType(type);
        if (a == null) {
            throw new Exception(type + " account not found");
        }
        a.withdraw(amount, description);
    }

    public void transferOwnAccounts(Account.AccountType fromType, Account.AccountType toType, double amount, String description) throws Exception {
        Account from = getAccountByType(fromType);
        Account to = getAccountByType(toType);

        if (from == null ) throw new Exception( fromType + " account not found");
        else if( to == null) throw new Exception( toType + " account not found");

        if (!from.canTransferAmount(amount, true)) {
            throw new Exception("Transfer Failed: Daily limit exceeded");
        }

        from.withdraw(amount, description + " [ " + amount +" Transferred from " + from.getAccountId()+ " Account to " + to.getAccountId()+  " ]");
        to.deposit(amount, description + " [ " + amount +" Transferred from " + from.getAccountId()+ " Account to " + to.getAccountId() + " ]" );

        saveCustomer(this);
    }

    public void transferToAnotherCustomer(Customer receiver,Account.AccountType fromType, Account.AccountType toType, double amount, String description) throws Exception {
    Account from = getAccountByType(fromType);
    if (from == null ) throw new Exception( "Your" +fromType + " account not found");

    Account to = receiver.getAccountByType(toType);
    if( to == null) throw new Exception( "Recipient " + toType + " account not found");

    if (!from.canTransferAmount(amount, false)) {
            throw new Exception("Transfer Failed: Daily limit exceeded");
    }

    from.withdraw(amount, description + " [ " + amount +" Transferred from (Customer " + from.getCustomerId() +" | Account: "+ from.getAccountId() +") to (Customer " + to.getCustomerId() +" | Account: "+ to.getAccountId() + ") ]");
    to.deposit(amount, description + " [ " +amount +" Transferred from (Customer " + from.getCustomerId() +" | Account: "+ from.getAccountId() +") to Your Account " + to.getAccountId() + " ]");

    saveCustomer(this);
    saveCustomer(receiver);
    }


public void displayFilterdTransactions(Customer c, String period) {
    System.out.println("------------------------------------Transaction History--------------------------------------");
    Set<Transaction> uniqueTransactions = new HashSet<>();
    List<Transaction> filteredTransactions = new ArrayList<>();
    LocalDate today = LocalDate.now();

    LocalDateTime start;
    LocalDateTime end = LocalDateTime.now();

    switch (period.toLowerCase()) {
        case "today":
            start = today.atStartOfDay();
            break;
        case "yesterday":
            start = today.minusDays(1).atStartOfDay();
            end = today.atStartOfDay();
            break;
        case "last 7 days":
            start = today.minusDays(7).atStartOfDay();
            break;
        case "last week":
            LocalDate lastWeekStart = today.minusWeeks(1).with(DayOfWeek.SUNDAY);
            LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
            start = lastWeekStart.atStartOfDay();
            end = lastWeekEnd.atTime(23, 59, 59);
            break;
        case "last 30 days":
            start = today.minusDays(30).atStartOfDay();
            break;
        case "last month":
            LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
            start = firstDayOfLastMonth.atStartOfDay();
            end = lastDayOfLastMonth.atTime(23, 59, 59);
            break;
        default:
            start = LocalDate.of(2025, 1, 1).atStartOfDay();
            System.out.println("All transactions.");

    }


    for (Account a : c.getAccounts()) {
        LocalDateTime ends = end;
        List<Transaction> accountTransactions = a.getTransactionHistory().stream()
                .filter(t -> !t.getDateTime().isBefore(start) && !t.getDateTime().isAfter(ends))
                .collect(Collectors.toList());

        for (Transaction t : accountTransactions) {
            if (!uniqueTransactions.contains(t)) {
                filteredTransactions.add(t);
                uniqueTransactions.add(t);
            }
        }
    }

    if (filteredTransactions.isEmpty()) {
        System.out.println("No Transactions at this time for this Customer");
        return;
    }
    filteredTransactions.sort(Comparator.comparing(Transaction::getDateTime));
    printTransactions(filteredTransactions);
}

    public void printTransactions(List<Transaction> txList) {
        System.out.println("Account        |     Date & Time     |    Type  | Amount | PostBalance | Description");
        System.out.println("----------------------------------------------------------------------------------------------");


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Transaction t : txList) {
            System.out.printf("%s | %s | %s | %.2f |  %.2f  | %s\n",
                    t.getAccountId(),
                    t.getDateTime().format(formatter),
                    t.getType(),
                    t.getAmount(),
                    t.getPostBalance(),
                    t.getDescription());
        }
    }

    public void displayAccountStatment(Customer c, int option) {
        System.out.println("\n-----------------------------------------Account Statement------------------------------------");
        boolean found = false;

        for (Account a : c.getAccounts()) {
            boolean includeAccount = false;
            switch (option) {
                case 1:
                    if (a instanceof CheckingAccount)  includeAccount = true;
                    break;

                    case 2:
                        if (a instanceof SavingAccount)  includeAccount = true;
                        break;

                        case  3:
                        includeAccount = true;
                        break;

                default:
                    System.out.println("Invalid option");
                    return;
            }

            if (!includeAccount) continue;

            found = true;
            System.out.println("\nAccount ID: " + a.getAccountId());
            System.out.println("Current Balance: " + a.getBalance());
            System.out.println("Card Type: " + a.getCard().getCardType());

            System.out.println("\nTransactions:");

            List<Transaction> list = a.getTransactionHistory();

            if(list.isEmpty() || list == null){
                System.out.println("No Transactions for this Account");
                continue;
            }
            printTransactions(list);

            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("Total Balance of this Account " + a.getAccountId()+": " +a.getBalance());

            if(!found){
                System.out.println("No accounts found for this selection");
            }

            System.out.println("----------------------------------------------------------------------------------------------");

        }

    }


}
