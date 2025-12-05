package com.bank.user;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class User {
    protected String id;
    protected String name;
    protected String password;
    protected Role role;

    private int failedAttempts = 0;
    private long lockTime = 0;

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getLockTime() {
        return lockTime;
    }


    public enum Role {
        Banker,
        Customer
    }

    public User(){}

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(String id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public boolean login(String name, String password) {
        if (isLocked()) {
            System.out.println("Account is locked. Try Again Later.");
            return false;
        }
        if (this.name.equals(name) && this.password.equals(password)) {
            failedAttempts = 0;
            updateUsersFile();
            return true;
        } else {
            failedAttempts++;
            System.out.println("Invalid username or password! Attempt " + failedAttempts + "/3.");
            if (failedAttempts >= 3) {
                lockTime = System.currentTimeMillis();
                System.out.println("Too many failed attempts.");
            }
            updateUsersFile();
            return false;
        }

    }

    public boolean isLocked() {
        if (lockTime == 0) return false;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lockTime >= 60000) {
            failedAttempts = 0;
            lockTime = 0;
            updateUsersFile();
            return false;
        }
        return true;
    }

    private static final String USERS_FILE = "src/data/users/data.txt";


//    public static User loadUser(String name, String password) {
//        try {
//
//            File fileObj = new File("data.txt");
//            Scanner reader = new Scanner(fileObj);
//
//            while (reader.hasNextLine()) {
//                String data = reader.nextLine();
//                String[] dataArray = data.split(",");
//                String Id = dataArray[0];
//                String Name = dataArray[1];
//                String Password = dataArray[2];
//                Role role = Role.valueOf(dataArray[3]);
//
//                User user = new User(Id, Name, Password, role );
//
//                if (user.login(name, password)) {
//                    reader.close();
//                    return user;
//                }
//
//                if (user.getName().equals(name)) {
//                    reader.close();
//                    return user;
//                }
//
//            }
//
//            reader.close();
//        } catch (FileNotFoundException e) {
//            System.out.println("File not found!");
//        }
//
//        return null;
//    }
//}

    public static User findUserByName(String name) {
        try {
            File fileObj = new File(USERS_FILE);
            if (!fileObj.exists()) return null;

            Scanner reader = new Scanner(fileObj);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] dataArray = data.split(",");
                String Id = dataArray[0];
                String Name = dataArray[1];
                String Password = dataArray[2];
                Role role = Role.valueOf(dataArray[3]);

                if (Name.equals(name)) {
                    reader.close();
                    return new User(Id, Name, Password, role);
                }
            }

            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
        return null;
    }

    public void updateUsersFile() {
        File file = new File(USERS_FILE);
        try {
            if(!file.exists()){
                file.getParentFile().mkdirs();

                List<String> lines = new java.util.ArrayList<>();
                if (file.exists()) {
                    Scanner reader = new Scanner(file);
                    while (reader.hasNextLine()) {
                        lines.add(reader.nextLine());
                    }
                    reader.close();
                }

                boolean found = false;
                for (int i = 0; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    if (parts[0].equals(this.id)) {
                        lines.set(i, id + "," + name + "," + password + "," + role);
                        found = true;
                        break;
                    }
                }
                if (!found) lines.add(id + "," + name + "," + password + "," + role);

                // write back
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
                for (String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
                bw.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

