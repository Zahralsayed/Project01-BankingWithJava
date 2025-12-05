//import com.bank.user.*;
//import java.util.Scanner;
//
//public class Main {
//    public static void main(String[] args) {
//
//        Scanner kbd = new Scanner(System.in);
//        System.out.println("Welcome to the Bank Management System");
//
//        try {
//
//            User currentUser = null;
////            User loggedInUser = null;
//
//            while (currentUser == null) {
////            while (loggedInUser == null) {
//                System.out.println("Enter User Name: ");
//                String name = kbd.nextLine();
//                System.out.println("Enter Password: ");
//                String password = kbd.nextLine();
//
//                loggedInUser = User.loadUser(name, password);
//
//                if (loggedInUser != null) {
//                    System.out.println("Login successful!");
//                    System.out.println("Role: " + loggedInUser.getRole());
//
//                    if (loggedInUser.getRole() == User.Role.Banker) {
//                        boolean bankerMenu = true;
//
//                        while (bankerMenu) {
//                            System.out.println("\nHello Banker!");
//                            System.out.println("--------CHOOSE OPTION----------");
//                            System.out.println("1) Create new Customer");
//                            System.out.println("2) Logout");
//                            String cmd = kbd.nextLine().trim();
//
//                            if (cmd.equals("1")) {
//                                System.out.print("Enter Customer ID: ");
//                                String cId = kbd.nextLine().trim();
//                                System.out.print("Enter Customer Name: ");
//                                String cName = kbd.nextLine().trim();
//
//                                String randomPassword = PasswordGenerator.generateRandomPassword();
//                                Customer c = new Customer(cId, cName, randomPassword, User.Role.Customer);
//
//                                System.out.print("Checking Account (Y/N)? ");
//                                String checking = kbd.nextLine();
//                                System.out.print("Savings Account (Y/N)? ");
//                                String savings = kbd.nextLine();
//
//                                // create accounts based on input here
//                                // e.g., c.createCheckingAccount(), c.createSavingsAccount()
//
//                                c.saveCustomer(c);
//                                System.out.println("Customer created Successfully");
//                                System.out.println("Customer Id: " + c.getId());
//                                System.out.println("Temp Password: " + c.getPassword());
//
//                            } else if (cmd.equals("2")) {
//                                System.out.println("BYE.. SEE YOU AGAIN!");
//                                bankerMenu = false;
//                            } else {
//                                System.out.println("Invalid option. Try again.");
//                            }
//                        }
//
//                    } else if (loggedInUser.getRole() == User.Role.Customer) {
//                        Customer c = Customer.loadCustomer(loggedInUser.getId());
//                        if (c == null) {
//                            System.out.println("Customer not found!");
//                            return;
//                        }
//
//                        if (c.isFirstLogin()) {
//                            System.out.println("First Login Successful!");
//                            System.out.print("Set Your Password: ");
//                            String newPassword = kbd.nextLine().trim();
//                            c.setPassword(newPassword);
//                            c.setFirstLogin(false);
//                            c.saveCustomer(c);
//                            c.updateCustomerLineInDataFile(c);
//                            System.out.println("Password updated successfully!");
//                        } else {
//                            System.out.println("Welcome back!");
//                        }
//
//                        boolean customerMenu = true;
//                        while (customerMenu) {
//                            System.out.println("\n--------Main Menu-------------");
//                            System.out.println("1) Create new Account");
//                            System.out.println("2) Deposit Money");
//                            System.out.println("3) Withdraw Money");
//                            System.out.println("4) Transfer Money");
//                            System.out.println("5) Transaction History");
//                            System.out.println("6) Account Statement");
//                            System.out.println("7) Logout");
//
//                            String cmd = kbd.nextLine().trim();
//
//                            switch (cmd) {
//                                case "1":
//                                    System.out.println("--Select Account Type--");
//                                    System.out.println("1) Checking Account");
//                                    System.out.println("2) Savings Account");
//                                    System.out.println("3) Both");
////                                    int option = Integer.parseInt(kbd.nextLine().trim());
////
////                                    System.out.print("Initial Balance: ");
////                                    double initialB = Double.parseDouble(kbd.nextLine().trim());
////                                    // call c.createAccount(option, initialB);
//                                    break;
//
//                                case "2":
//                                    // deposit logic
//                                    break;
//
//                                case "3":
//                                    // withdraw logic
//                                    break;
//
//                                case "4":
//                                    // transfer logic
//                                    break;
//
//                                case "5":
//                                    // transaction history
//                                    break;
//
//                                case "6":
//                                    // account statement
//                                    break;
//
//                                case "7":
//                                    System.out.println("Logging out...");
//                                    customerMenu = false;
//                                    break;
//
//                                default:
//                                    System.out.println("Invalid option. Try again.");
//                            }
//                        }
//                    }
//
//                } else {
//                    System.out.println("Login failed. Please try again.");
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

// ------------------------------------------------------------------------------------------------------------


import com.bank.accounts.Account;
import com.bank.accounts.CardType;
import com.bank.accounts.CheckingAccount;
import com.bank.accounts.SavingAccount;
import com.bank.user.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner kbd = new Scanner(System.in);
        System.out.println("Welcome to the Bank Management System");

        try {

            User currentUser = null;

            // --- LOGIN LOOP ---
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
                        System.out.println("Account is locked. Please wait 1 minute.");
                        break;
                    }

                    System.out.print("Enter Password: ");
                    String password = kbd.nextLine().trim();

                    if (tempUser.login(name, password)) {
                        System.out.println("Login successful! Role: " + tempUser.getRole());
                        loggedIn = true;
                        currentUser = tempUser;

                    }
                }
            }

            // --- POST-LOGIN MENU ---
            if (currentUser.getRole() == User.Role.Banker) {
                boolean bankerMenu = true;

                while (bankerMenu) {
                    System.out.println("\nHello Banker!");
                    System.out.println("--------CHOOSE OPTION----------");
                    System.out.println("1) Create new Customer");
                    System.out.println("2) Logout");
                    String cmd = kbd.nextLine().trim();

                    if (cmd.equals("1")) {
                        System.out.print("Enter Customer ID: ");
                        String cId = kbd.nextLine().trim();
                        System.out.print("Enter Customer Name: ");
                        String cName = kbd.nextLine().trim();

                        String randomPassword = PasswordGenerator.generateRandomPassword();
                        Customer c = new Customer(cId, cName, randomPassword, User.Role.Customer);

//                        c.saveCustomer(c); //1
                        String checkingNum = Account.generateAccountNumber(Account.AccountType.Checking);
                        CheckingAccount checking = new CheckingAccount(checkingNum, c.getId(), 0.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
                        c.setCheckingAccount(checking);
                        c.addAccount(checkingNum);
                        c.getAccounts().add(checking);

                        c.saveAsJson();

                        c.appendToDataFile(c);

                        System.out.println("Customer created Successfully");
                        System.out.println("Customer Id: " + c.getId());
                        System.out.println("Temp Password: " + c.getPassword());

                    } else if (cmd.equals("2")) {
                        System.out.println("BYE.. SEE YOU AGAIN!");
                        bankerMenu = false;
                    } else {
                        System.out.println("Invalid option. Try again.");
                    }
                }

            } else if (currentUser.getRole() == User.Role.Customer) {
//                Customer c = Customer.loadCustomer(currentUser.getId()); //1
                Customer c = Customer.loadFromJson(currentUser.getId());
                if (c == null) {
                    System.out.println("Customer not found!");
                    return;
                }

                if (c.isFirstLogin()) {
                    System.out.println("It's Your First Time To Login!");
                    System.out.print("Please Set Your Password: ");
                    String newPassword = kbd.nextLine().trim();
                    c.setPassword(newPassword);
                    c.setFirstLogin(false);
//                    c.saveCustomer(c); //1
//                    c.updateCustomerLineInDataFile(c); //1
                    c.saveAsJson();
                    System.out.println("Password updated successfully!");
                } else {
                    System.out.println("Welcome back!");
                }

                boolean customerMenu = true;
                while (customerMenu) {
                    System.out.println("\n--------Main Menu-------------");
//                    if(c.getSavingAccount() == null) {
                        System.out.println("1) Deposit Money");
                        System.out.println("2) Withdraw Money");
                        System.out.println("3) Transfer Money");
                        System.out.println("4) Transaction History");
                        System.out.println("5) Account Statement");
                        System.out.println("6) Logout");
                    if (c.getSavingAccount() == null) {
                        System.out.println("7) Create Saving Account");
                    }
                    String cmd = kbd.nextLine().trim();

                    switch (cmd) {
                        case "1":
//                            System.out.println("--Select Account Type--");
//                            System.out.println("1) Checking Account");
//                            System.out.println("2) Savings Account");
//                            System.out.println("3) Both");
//                            int option = Integer.parseInt(kbd.nextLine().trim());
//                            System.out.print("Initial Balance: ");
//                            double initialB = Double.parseDouble(kbd.nextLine().trim());
//                            // call c.createAccount(option, initialB);


                            // deposit logic
                            break;

                        case "2":
                            // withdraw logic

                            break;

                        case "3":
                            // transfer logic
                            break;

                        case "4":
                            // transaction history

                            break;

                        case "5":
                            // account statement

                            break;

                        case "6":
                            System.out.println("Logging out...");
                            System.out.println("BYE.. SEE YOU AGAIN!");
                            customerMenu = false;
                            break;


                        case "7":
                            if (c.getSavingAccount() == null) {
                                String accNum = Account.generateAccountNumber(Account.AccountType.Saving);
                                SavingAccount saving = new SavingAccount(accNum,c.getId(),0.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
                                c.setSavingAccount(saving);
                                c.addAccount(accNum);
                                c.getAccounts().add(saving);
                                c.saveAsJson();
//                                checking.save(); //to save in File
                                System.out.println("Saving account created successfully");
                                System.out.println("Saving account Id: " + c.getSavingAccount().getAccountId() + ", Balance: " + c.getSavingAccount().getBalance());


                            } else {
                        System.out.println("You already have a Saving account!");
                    }


//                            c.saveCustomer(c); //1
                            break;

                        default:
                            System.out.println("Invalid option. Try again.");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            kbd.close();
        }
    }
}
