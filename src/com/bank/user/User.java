package com.bank.user;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class User {
    protected String id;
    protected String name;
    protected String password;
    protected Role role;

    private int failedAttempts;
    private long lockTime;

    private static final String USERS_DIR = "src/data/users";

    public enum Role {
        Banker,
        Customer
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    // Constructor for new users
    public User(String id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
        this.failedAttempts = 0;
        this.lockTime = 0;
    }

    // reading data from data.txt
    public User(String id, String name, String password, Role role, int failedAttempts, long lockTime) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
        this.failedAttempts = failedAttempts;
        this.lockTime = lockTime;
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

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getLockTime() {
        return lockTime;
    }


    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public boolean isLocked() {
        long currentTime = System.currentTimeMillis();
        if (lockTime == 0) return false;

        if (currentTime >= lockTime) {
            failedAttempts = 0;
            lockTime = 0;
            return false;
        }

        long remaining = (lockTime - currentTime) / 1000;
        System.out.println("Account is locked. Try again in " + remaining + " seconds.\n");
        return true;
    }

    public boolean login(String name, String password) {
        if (isLocked()) {
            System.out.println("Account is locked. Try Again Later.");
            updateDataFile();
            return false;
        }

        try {

            if (this.name.equals(name) && this.password.equals(password)) {
                this.failedAttempts = 0;
                this.lockTime = 0;
                updateDataFile();
                return true;

            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        failedAttempts++;
            System.out.println("Invalid username or password! Attempt " + failedAttempts + "/3.");
            if (failedAttempts >= 3) {
                lockTime = System.currentTimeMillis() + 60_000;
                System.out.println("Too many failed attempts.");
            }
            updateDataFile();
            return false;


    }


    public static User findUserByName(String name) {
        try {
            File fileObj = new File(USERS_DIR +"/data.txt");
            Scanner reader = new Scanner(fileObj);

            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] dataArray = data.split(",");
                String Id = dataArray[0];
                String Name = dataArray[1];
                String Password = dataArray[2];
                Role role = Role.valueOf(dataArray[3]);
                int failedAttempts = Integer.parseInt(dataArray[4]);
                long lockUntil = Long.parseLong(dataArray[5]);

                if (Name.equals(name)) {
                    reader.close();
                    return new User(Id, Name, Password, role, failedAttempts, lockUntil);
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateCustomerId() {
        long time = System.currentTimeMillis() % 1_000_000;
        return "C2025-" + String.format("%04d", time);
    }

    private void updateDataFile() {
        File file = new File(USERS_DIR + "/data.txt");
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try {
            if (file.exists()) {
                try (Scanner reader = new Scanner(file)) {
                    while (reader.hasNextLine()) {
                        String line = reader.nextLine();
                        String[] parts = line.split(",");
                        if (parts.length >= 6 && parts[0].equals(this.id)) {

                            line = String.join(",", this.id, this.name, this.password,
                                    this.role.toString(),
                                    String.valueOf(this.failedAttempts),
                                    String.valueOf(this.lockTime));
                            found = true;
                        }
                        lines.add(line);
                    }
                }
            }

            if (!found) {
                lines.add(String.join(",", this.id, this.name, this.password,
                        this.role.toString(),
                        String.valueOf(this.failedAttempts),
                        String.valueOf(this.lockTime)));
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                for (String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}


