package com.bank.user;
import com.bank.accounts.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.bank.accounts.CardType.*;

public class Customer extends User {
    protected SavingAccount savingAccount; //1
    protected CheckingAccount checkingAccount;  //1
    protected List<String> accountNumbers = new ArrayList<>(); //1
    protected List<Account> accounts = new ArrayList<>();
    protected List<Transaction> transactions = new ArrayList<>();
    protected int failedAttempts = 0;
    protected long lockUntil = 0;
    protected boolean firstLogin = true;
    protected boolean locked = false;

    private static final String USERS_DIR = "src/data/users";

    public Customer() {}
    public Customer(String id, String name, String password, Role role) {
        super(id, name, password, Role.Customer);
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

//    public void addAccount(String accountNumber) {
//        if(!accountNumbers.contains(accountNumber)) {
//            accountNumbers.add(accountNumber);
//        }
//    } // 1

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void addAccount(String accountNumber) {
        if(!accountNumbers.contains(accountNumber)) {
            accountNumbers.add(accountNumber);
        }
    }

    public void removeAccount(String accountNumber) {
        accountNumbers.remove(accountNumber);
    } //1

    public long getLockUntil() {
        return lockUntil;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public void setAccountNumbers(List<String> accountNumbers) {
        this.accountNumbers = accountNumbers;
    }//1

    public void setLockUntil(long lockUntil) {
        this.lockUntil = lockUntil;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Account getSavingAccount() {
        return savingAccount;
    }

    public void setSavingAccount(SavingAccount savingAccount) {
        this.savingAccount = savingAccount;
    }

    public Account getCheckingAccount() {
        return checkingAccount;
    }

    public void setCheckingAccount(CheckingAccount checkingAccount) {
        this.checkingAccount = checkingAccount;
    } //1

//    public List<Transaction> getTransactions() {
//        return transactions;
//    }
//
//    public void setTransactions(List<Transaction> transactions) {
//        this.transactions = transactions;
//    } //1

    private String safeName(String n) {
        return n.replaceAll("\s+", "_");
    }

    public void saveAsJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Path dir = Paths.get(USERS_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String filePath = USERS_DIR + "/Customer-" + safeName(getName()) + "-" + getId() + ".json";
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), this);

            System.out.println("Customer saved as JSON: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Customer loadFromJson(String id) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File folder = new File(USERS_DIR);
            if (!folder.exists() || !folder.isDirectory()) return null;

            for (File f : folder.listFiles()) {
                System.out.println("Checking file: " + f.getName());
                if (f.getName().contains(id) && f.getName().endsWith(".json")) {
                    return mapper.readValue(f, Customer.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Optional<Account> getAccountByType(Account.AccountType type) {
        return accounts.stream()
                .filter(a -> a.getAccountType().equals(type)).findFirst();
    }


    public void addTransaction(Transaction t) {
        transactions.add(t); // master list
        // also push to the account's own transaction history
        for (Account a : accounts) {
            if (a.getAccountId().equals(t.getAccountId())) {
                a.addTransaction(t);
                break;
            }
        }
    }

    public List<Transaction> getTransactionsForAccount(String accountId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getAccountId().equals(accountId)) {
                result.add(t);
            }
        }
        return result;
    }



//
//    public void saveCustomer(Customer c) {
//
//        if (c.getCheckingAccount() == null) {
//            String accNum = Account.generateAccountNumber(Account.AccountType.Checking);
//            Account checking = new CheckingAccount(accNum,c.getId(),0.0, new CardType(DebitCardType.Standard_Mastercard));
//            c.setCheckingAccount(checking);
//            c.addAccount(accNum);
////            checking.save(); //to save in File
//        }
//
//        String file =  USERS_DIR + "/Customer-" + safeName(c.getName()) + "-" + c.getId() + ".txt";
//        String accs = String.join(",", c.getAccountNumbers());
//        String line = String.join(",",
//                c.getId(), c.getName(), c.getPassword(), accs, String.valueOf(c.getfailedAttempts()), String.valueOf(c.getLockUntil()), String.valueOf(c.isFirstLogin())
//        );
//
//        String data = String.join(",",
//                c.getId(), c.getName(), c.getPassword(), String.valueOf(c.getRole()));
//        try {
//////            FileWriter f = new FileWriter("data.txt", true);
////            Files.write(Paths.get(file), Arrays.asList(line));
//////            f.write("\n"+ data);
//////            f.close();
//
//            Path dir = Paths.get(USERS_DIR);
//            if (!Files.exists(dir)) {
//                Files.createDirectories(dir);
//            }
//
//            // 2. Write file (creates if not exists, overwrites if exists)
//            Files.write(Paths.get(file), Arrays.asList(line));
//
//            System.out.println("Saved: " + Paths.get(file).toAbsolutePath());
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        appendToDataFile(c);
//    }
//
    public void appendToDataFile(Customer c) {
        String data = String.join(",",
                c.getId(), c.getName(), c.getPassword(), String.valueOf(c.getRole())
        );

        try (FileWriter f = new FileWriter(USERS_DIR+ "/data.txt", true)) {
            f.write("\n" +data );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
//
//    public void updateCustomerLineInDataFile(Customer updated) {
//        File file = new File("data.txt");
//        List<String> lines = new ArrayList<>();
//
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(",");
//                if (parts[0].equals(updated.getId())) {
//                    line = updated.getId() + "," +
//                            updated.getName() + "," +
//                            updated.getPassword() + "," +
//                            updated.getRole();
//                }
//                lines.add(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
//            for (String l : lines) {
//                bw.write(l);
//                bw.newLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

public void updateCustomerLineInDataFile(Customer updated) {
    File file = new File(USERS_DIR + "/data.txt");
    List<String> lines = new ArrayList<>();
    boolean found = false;

    // Read existing lines
    if (file.exists()) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].equals(updated.getId())) {
                    // Replace line with updated info
                    line = String.join(",",
                            updated.getId(),
                            updated.getName(),
                            updated.getPassword(),
                            String.valueOf(updated.getRole())
                    );
                    found = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // If user not found, add a new line
    if (!found) {
        String newLine = String.join(",",
                updated.getId(),
                updated.getName(),
                updated.getPassword(),
                String.valueOf(updated.getRole())
        );
        lines.add(newLine);
    }

    // Write all lines back
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
        for (String l : lines) {
            bw.write(l);
            bw.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


//
//
//    public static Customer loadCustomer(String id) {
//
//        File folder = new File(USERS_DIR);
//        if (!folder.exists() || !folder.isDirectory()) {
//            System.out.println("Users directory not found!");
//            return null;
//        }
//
//        for (File f : folder.listFiles()) {
//            if (f.getName().contains(id)) {
//
//                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
//
//                    String line = br.readLine();
//                    if (line == null || line.isEmpty())
//                        return null;
//
//                    String[] p = line.split(",");
//
//                    String custId          = p[0];
//                    String name            = p[1];
//                    String password        = p[2];
//
//                    List<String> accounts = new ArrayList<>();
//                    if (!p[3].isEmpty()) {
//                        accounts = Arrays.asList(p[3].split(","));
//                    }
//
//                    int failedAttempts     = Integer.parseInt(p[4]);
//                    long lockUntil         = Long.parseLong(p[5]);
//                    boolean firstLogin     = Boolean.parseBoolean(p[6]);
//
//
//                    Customer c = new Customer(custId, name, password, User.Role.Customer);
//                    c.setAccountNumbers(new ArrayList<>(accounts));
//                    c.setfailedAttempts(failedAttempts);
//                    c.setLockUntil(lockUntil);
//                    c.setFirstLogin(firstLogin);
//
//                    return c;
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        }
//
//        return null;
//    } //1



}
