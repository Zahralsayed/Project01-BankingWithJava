import com.bank.accounts.*;
import com.bank.user.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner kbd = new Scanner(System.in);
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("                            Welcome to the Bank Management System");
        System.out.println("----------------------------------------------------------------------------------------------");


        try {

            User currentUser = null;


            while (currentUser == null) {
                System.out.print("Enter User Name: ");
                String name = kbd.nextLine().trim();

                User tempUser = User.findUserByName(name);

                if (tempUser == null) {
                    System.out.println("Username not found. Try again.");
                    continue;
                }

                boolean loggedIn = false;
                while (!loggedIn) {

                    if (tempUser.isLocked()) {
                        break;
                    }

                    System.out.print("Enter Password: ");
                    String password = kbd.nextLine().trim();

                    String hashedInput = PasswordHash.hashPassword(password);

                    if (tempUser.login(name, hashedInput)) {
                        System.out.println("Login successful! Role: " + tempUser.getRole());
                        loggedIn = true;
                        currentUser = tempUser;
                    }
                }
            }


            if (currentUser.getRole() == User.Role.Banker) {
                boolean bankerMenu = true;

                while (bankerMenu) {
                    System.out.println("\nHello Banker!");
                    System.out.println("----------------------------------------CHOOSE OPTION-----------------------------------------");
                    System.out.println("1) Create new Customer");
                    System.out.println("2) Logout");
                    String cmd = kbd.nextLine().trim();

                    switch (cmd) {
                        case "1":
                            String cId = User.generateCustomerId();
                            System.out.print("Enter Customer Name: ");
                            String cName = kbd.nextLine().trim();

                            String randomPassword = PasswordGenerator.generateRandomPassword();
                            String hashedPassword = PasswordHash.hashPassword(randomPassword);
                            Customer c = new Customer(cId, cName, hashedPassword, User.Role.Customer);

                            String checkingNum = Account.generateAccountNumber(Account.AccountType.Checking);
                            CheckingAccount checking = new CheckingAccount(checkingNum, c.getId(), 0.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
                            c.addAccountObject(checking);
                            c.saveCustomer(c);

                            System.out.println("Customer created Successfully");
                            System.out.println("Customer Id: " + c.getId());
                            System.out.println("Temp Password: " + randomPassword);

                            break;
                        case "2":
                            System.out.println("BYE.. SEE YOU AGAIN!");
                            bankerMenu = false;
                            break;

                        default:
                            System.out.println("Invalid option. Try again.");

                    }
                }

            } else if (currentUser.getRole() == User.Role.Customer) {
                Customer c = Customer.loadCustomer(currentUser.getId()); //1

                if (c == null) {
                    System.out.println("Customer not found!");
                    return;
                }

                if (c.isFirstLogin()) {
                    System.out.println("It's Your First Time To Login!");
                    System.out.print("Please Set Your Password: ");
                    String newPassword = kbd.nextLine().trim();
                    String hashedPassword = PasswordHash.hashPassword(newPassword);
                    c.setPassword(hashedPassword);
                    c.setFirstLogin(false);
                    c.saveCustomer(c);

                    System.out.println("Password updated successfully!");
                } else {
                    System.out.println("Welcome back!");
                }

                boolean customerMenu = true;
                while (customerMenu) {
                    System.out.println("\n-----------------------------------------Main Menu--------------------------------------------");

                        System.out.println("1) Deposit Money");
                        System.out.println("2) Withdraw Money");
                        System.out.println("3) Transfer Money");
                        System.out.println("4) Transaction History");
                        System.out.println("5) Account Statement");
                        System.out.println("6) Upgrade Your Card Type");
                        System.out.println("7) Logout");
                    if (c.getSavingAccount() == null) {
                        System.out.println("8) Create Saving Account");
                    }
                    String cmd = kbd.nextLine().trim();

                    switch (cmd) {
                        case "1":
                            System.out.println("--Deposit to (Select Account Type) --");
                            System.out.println("1) Checking Account");
                            System.out.println("2) Savings Account");
                            String depositOption = kbd.nextLine().trim();
                            Account.AccountType depositType = depositOption.equals("1") ? Account.AccountType.Checking : Account.AccountType.Saving;
                            System.out.print("Amount to Deposit: ");
                            double depositAmount = kbd.nextDouble();
                            kbd.nextLine();

                            System.out.print("Transaction Description: ");
                            String transactionDescription = kbd.nextLine().trim();

                            Account targetAcc = c.getAccountByType(depositType);
                            if (targetAcc == null) {
                                System.out.println("Selected account not found.");
                                break;
                            }

                            boolean ownDeposit = true;
                            if (!targetAcc.canDepositAmount(depositAmount, ownDeposit)) {
                                break;
                            }

                            try {
                                c.depositToAccount(depositType, depositAmount, transactionDescription);
                                c.saveCustomer(c);
                                System.out.println("Deposited Successfully!");
                            } catch (Exception e) {
                                System.out.println("Deposit failed: " + e.getMessage());
                            }

                            break;

                        case "2":
                            System.out.println("--Withdraw from (Select Account Type) --");
                            System.out.println("1) Checking Account");
                            System.out.println("2) Savings Account");
                            String withdrawOption = kbd.nextLine().trim();
                            Account.AccountType withdrawType = withdrawOption.equals("1") ? Account.AccountType.Checking : Account.AccountType.Saving;
                            System.out.print("Amount to Withdraw: ");
                            double withdrawAmount = kbd.nextDouble();
                            kbd.nextLine();

                            System.out.print("Transaction Description: ");
                            String withdrawDescription = kbd.nextLine().trim();

                            Account fromAcc = c.getAccountByType(withdrawType);
                            if (fromAcc == null) {
                                System.out.println("Selected account not found.");
                                break;
                            }

                            if (!fromAcc.canWithdrawAmount(withdrawAmount)) {
                                break;
                            }

                            try {
                                c.withdrawFromAccount(withdrawType, withdrawAmount, withdrawDescription);
                                c.saveCustomer(c);
                                System.out.println("Withdraw successful!");
                            } catch (Exception e) {
                                System.out.println("Withdraw failed: " + e.getMessage());
                            }
                            break;

                        case "3":
                            System.out.println("--Transfer Money (Select Transfer Type)--");
                            System.out.println("1) Transfer Between My Accounts");
                            System.out.println("2) Transfer To Another Customer");
                            String transferOption = kbd.nextLine().trim();
                            if (transferOption.equals("1")) {
                                System.out.println("--Transfer Between My Accounts--");
                                System.out.println("Transfer From: ");
                                System.out.println("1) Checking Account");
                                System.out.println("2) Savings Account");
                                String transferFromOption = kbd.nextLine().trim();

                                Account.AccountType transferFromType;
                                if (transferFromOption.equals("1")) {
                                    transferFromType = Account.AccountType.Checking;
                                } else if (transferFromOption.equals("2")){
                                    transferFromType = Account.AccountType.Saving;
                                } else {
                                    System.out.println("Invalid Selection.");
                                    break;
                                }

                                System.out.println("Transfer TO:");
                                if (transferFromType ==  Account.AccountType.Checking) {
                                    System.out.println("1) Savings Account");
                                }
                                else {
                                    System.out.println("1) Checking Account");
                                }
                                String transferToOption = kbd.nextLine().trim();


                                Account.AccountType transferToType;
                                if (transferFromType == Account.AccountType.Checking && transferToOption.equals("1")) {
                                    transferToType = Account.AccountType.Saving;
                                } else if (transferFromType == Account.AccountType.Saving && transferToOption.equals("1")) {
                                    transferToType = Account.AccountType.Checking;
                                } else { System.out.println("Invalid Transfer Option.");
                                    break;
                                }

                                System.out.println("Enter Amount to Transfer: ");
                                double transferAmount = kbd.nextDouble();
                                kbd.nextLine();

                                System.out.println("Transaction Description: ");
                                String transferDescription = kbd.nextLine().trim();

                                Account from = c.getAccountByType(transferFromType);
                                if(!from.canTransferAmount(transferAmount, true)){
                                    System.out.println("Transfer Failed due to daily limit.");
                                    return;
                                }

                                try {
                                    c.transferOwnAccounts(transferFromType, transferToType, transferAmount, transferDescription);
                                    c.saveCustomer(c);
                                    System.out.println("Amount transferred successfully!");
                                } catch (Exception e) {
                                    System.out.println("Transfer Failed!" + e.getMessage()  );
                                }
                            } else if (transferOption.equals("2")) {
                                System.out.println("--Transfer To Another Customer--");
                                System.out.println("Transfer From: ");
                                System.out.println("1) Checking Account");
                                System.out.println("2) Savings Account");
                                String transferFromOption = kbd.nextLine().trim();

                                Account.AccountType transferFromType;
                                if (transferFromOption.equals("1")) {
                                    transferFromType = Account.AccountType.Checking;
                                } else if (transferFromOption.equals("2")){
                                    transferFromType = Account.AccountType.Saving;
                                } else {
                                    System.out.println("Invalid Selection.");
                                    break;
                                }

                                System.out.println("Enter Receiver Customer ID: ");
                                String rId = kbd.nextLine().trim();

                                Customer receiver = Customer.loadCustomer(rId);
                                if (receiver == null) {
                                    System.out.println("Customer Not Found, Invalid Customer ID.");
                                    break;
                                }

                                System.out.println("Transfer TO Receiver:");
                                System.out.println("1) Checking Account");
                                System.out.println("2) Savings Account");
                                String transferToOption = kbd.nextLine().trim();

                                Account.AccountType transferToType;
                                if (transferToOption.equals("1")) {
                                    transferToType = Account.AccountType.Checking;
                                } else if (transferToOption.equals("2")){
                                    transferToType = Account.AccountType.Saving;
                                } else {
                                    System.out.println("Invalid Selection.");
                                    break;
                                }

                                System.out.println("Enter Amount to Transfer: ");
                                double transferAmount = kbd.nextDouble();
                                kbd.nextLine();

                                System.out.println("Transaction Description: ");
                                String transferDescription = kbd.nextLine().trim();

                                Account from = c.getAccountByType(transferFromType);
                                if(!from.canTransferAmount(transferAmount, false)){
                                    System.out.println("Transfer Failed due to daily limit.");
                                    return;                                }

                                try {
                                    c.transferToAnotherCustomer(receiver, transferFromType, transferToType, transferAmount, transferDescription);
                                    c.saveCustomer(c);
                                    System.out.println("Amount transferred successfully!");
                                } catch (Exception e) {
                                    System.out.println("Transfer Failed!" + e.getMessage()  );
                                }



                            }
                            break;

                        case "4":
                            if (currentUser == null){
                                System.out.println("You need to login first!");
                                break;
                            }


                            System.out.println("Filter transaction: ");
                            System.out.println("1) Today");
                            System.out.println("2) Yesterday");
                            System.out.println("3) Last 7 days");
                            System.out.println("4) Last Week");
                            System.out.println("5) Last 30 Days");
                            System.out.println("6) Last Month");
                            System.out.println("7) All");

                            String option = kbd.nextLine().trim();
                            String period = switch (option){
                                case "1" -> "today";
                                case "2" -> "yesterday";
                                case "3" -> "last 7 days";
                                case "4" -> "last week";
                                case "5" -> "last 30 days";
                                case "6" -> "last month";
                                default -> "all";
                            };

                            c.displayFilterdTransactions(c, period);
                            break;

                        case "5":
                            // account statement
//                            It Should return the total amount in the account and transactions with date and time.
                            System.out.println("----------------------------------------CHOOSE OPTION-----------------------------------------");
                            System.out.println("1) Checking Account Statement");
                            System.out.println("2) Savings Account Statement");
                            System.out.println("3) Both");
                            int statement = kbd.nextInt();
                            kbd.nextLine();

                            c.displayAccountStatment(c, statement);
                            break;

                            case "6":
                                System.out.println("Select Account to change the card");
                                System.out.println("1) Checking Account");
                                System.out.println("2) Savings Account");

                                String accountOption = kbd.nextLine().trim();
                                Account selectedAccount;

                                if (accountOption.equals("1")) {
                                    selectedAccount = c.getCheckingAccount();
                                } else if (accountOption.equals("2")) {
                                    selectedAccount = c.getSavingAccount();
                                } else {
                                    System.out.println("Invalid Account.");
                                    break;
                                }

                                if (selectedAccount == null) {
                                    System.out.println("Account Not Found, Please Select Another Option");
                                    break;
                                }

                                System.out.println("Select Card Type:");
                                System.out.println("1) Platinum Mastercard");
                                System.out.println("2) Titanium Mastercard");
                                System.out.println("3) Standard Mastercard");

                                String cardOption = kbd.nextLine().trim();
                                CardType.DebitCardType newType = null;

                                switch(cardOption) {
                                    case "1":
                                        newType = CardType.DebitCardType.Platinum_Mastercard;
                                        break;
                                    case "2":
                                        newType = CardType.DebitCardType.Titanium_Mastercard;
                                        break;
                                    case "3":
                                        newType = CardType.DebitCardType.Standard_Mastercard;
                                        break;
                                    default:
                                        System.out.println("Invalid card type.");
                                        break;
                                }

                                selectedAccount.setCard(new CardType(newType));
                                System.out.println("Card Type updated to: " +newType);
                                c.saveCustomer(c);
                                break;


                        case "7":
                            System.out.println("Logging out...");
                            System.out.println("BYE.. SEE YOU AGAIN!");
                            customerMenu = false;
                            break;


                        case "8":
                            if (c.getSavingAccount() == null) {
                                String accNum = Account.generateAccountNumber(Account.AccountType.Saving);
                                Account saving = new SavingAccount(accNum,c.getId(),0.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
                                c.setSavingAccount(saving);

                                c.addAccountObject(saving);
                                c.saveCustomer(c);

                                System.out.println("Saving account created successfully");
                                System.out.println("Saving account Id: " + c.getSavingAccount().getAccountId() + ", Balance: " + c.getSavingAccount().getBalance());
                            } else {
                        System.out.println("You already have a Saving account!");
                    }
                            break;

                        default:
                            System.out.println("Invalid option. Try again.");
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            kbd.close();
        }
    }


}
