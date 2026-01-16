package com.pluralsight;

import java.util.Scanner;
import java.io.*;           // file read/write
import java.math.BigDecimal; // money numbers
import java.time.*;         // date/time
import java.util.*;

public class accountingLedgerApplication {

    public static final Scanner Scanner = new Scanner(System.in); // input reader
    public static final String CSV_PATH = "transactions.csv"; // csv file name
    public static final ArrayList<data> ALL = new ArrayList<>(); // all transactions

    public static void main(String[] args) {
        loadCsv();

        while (true) { // main menu
            System.out.print("""
                
                Welcome To The Bagel Cafe:
                Please select one of the options below 
                
                D) Add Deposit 
                P) Make Payment (Debit)
                L) Ledger - display the ledger screen
                X) Exit - exit the application
                Choose: """
            );

            String choice = Scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "D":
                    addDeposit(); // add money
                    break;
                case "P":
                    addPayment(); // spend money
                    break;
                case "L":
                    ledgerMenu(); // open ledger
                    break;
                case "X":
                    System.out.println("Goodbye!");
                    return; // stop program
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    public static void addDeposit() { // deposit
        System.out.print("Description: ");
        String desc = Scanner.nextLine().trim();
        System.out.print("Vendor: ");
        String vendor = Scanner.nextLine().trim();
        BigDecimal Check = readMoney("Amount (positive): ");
        if (Check.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        addRow(desc, vendor, Check); // add to list
        System.out.println("Deposit saved.");
    }

    public static void addPayment() { // payment
        System.out.print("Description: ");
        String desc = Scanner.nextLine().trim();
        System.out.print("Vendor: ");
        String vendor = Scanner.nextLine().trim();
        BigDecimal check = readMoney("Amount (positive): ");
        if (check.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        addRow(desc, vendor, check.negate()); // make negative
        System.out.println("Payment saved.");
    }

    public static void ledgerMenu() { // ledger screen
        while (true) {
            System.out.println("""
                
                Ledger Menu:
                A) All - Display all entries
                D) Deposits - Display only the entries that are deposits into the account
                P) Payments - Display only the negative entries (or payments)
                R) Reports - Run pre-defined reports or a custom search
                H) Home - go back to the home page
                """);

            System.out.print("Choose: ");
            String choice = Scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                    showAll(); // show everything
                    break;
                case "D":
                    showDeposits(); // only deposits
                    break;
                case "P":
                    showPayments(); // only payments
                    break;
                case "R":
                    reportsMenu(); // open reports
                    break;
                case "H":
                    return; // go back
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    public static void addRow(String desc, String vendor, BigDecimal amount) { // add new row
        data row = data.now(desc, vendor, amount);
        ALL.add(row);
        appendCsv(row);
    }

    public static BigDecimal readMoney(String prompt) { // read valid number
        while (true) {
            System.out.print(prompt);
            String s = Scanner.nextLine().trim();
            try {
                return new BigDecimal(s);
            } catch (Exception e) {
                System.out.println("Enter a valid number (e.g., 123.45).");
            }
        }
    }

    public static void loadCsv() { // load file
        File f = new File(CSV_PATH);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                data row = data.fromCsv(line);
                if (row != null) ALL.add(row);
            }
        } catch (IOException ex) {
            System.err.println("Read error: " + ex.getMessage());
        }
    }

    public static void appendCsv(data row) { // write to file
        try (PrintWriter out = new PrintWriter(new FileWriter(CSV_PATH, true))) {
            out.println(row.toCsv());
        } catch (IOException ex) {
            System.err.println("Write error: " + ex.getMessage());
        }
    }

    public static void showAll() { // show everything
        if (ALL.isEmpty()) {
            System.out.println("\n(no entries)\n");
            return;
        }
        ArrayList<data> sorted = new ArrayList<>(ALL);
        sorted.sort(Comparator.comparing(data::getDate)
                .thenComparing(data::getTime)
                .reversed());
        System.out.println("\nDate       | Time     | Description          | Vendor            | Amount");
        System.out.println("-----------+----------+----------------------+-------------------+-----------");
        for (data d : sorted) {
            System.out.printf("%s | %-8s | %-20s | %-17s | %s%n",
                    d.getDate(), d.getTime(), d.getDescription(),
                    d.getVendor(), d.getAmount().toPlainString());
        }
        System.out.println();
    }

    public static void showDeposits() { // show positive
        ArrayList<data> deposits = new ArrayList<>();
        for (data d : ALL) {
            if (d.getAmount().compareTo(BigDecimal.ZERO) > 0) deposits.add(d);
        }
        if (deposits.isEmpty()) {
            System.out.println("\n(No deposit entries found)\n");
            return;
        }
        deposits.sort(Comparator.comparing(data::getDate)
                .thenComparing(data::getTime)
                .reversed());
        System.out.println("\nDate       | Time     | Description          | Vendor            | Amount");
        System.out.println("-----------+----------+----------------------+-------------------+-----------");
        for (data d : deposits) {
            System.out.printf("%s | %-8s | %-20s | %-17s | %s%n",
                    d.getDate(), d.getTime(), d.getDescription(),
                    d.getVendor(), d.getAmount().toPlainString());
        }
        System.out.println();
    }

    public static void showPayments() { // show negative
        ArrayList<data> payments = new ArrayList<>();
        for (data d : ALL) {
            if (d.getAmount().compareTo(BigDecimal.ZERO) < 0) payments.add(d);
        }
        if (payments.isEmpty()) {
            System.out.println("\n(No payment entries found)\n");
            return;
        }
        payments.sort(Comparator.comparing(data::getDate)
                .thenComparing(data::getTime)
                .reversed());
        System.out.println("\nDate       | Time     | Description          | Vendor            | Amount");
        System.out.println("-----------+----------+----------------------+-------------------+-----------");
        for (data d : payments) {
            System.out.printf("%s | %-8s | %-20s | %-17s | %s%n",
                    d.getDate(), d.getTime(), d.getDescription(),
                    d.getVendor(), d.getAmount().toPlainString());
        }
        System.out.println();
    }

    public static void reportsMenu() { // reports
        while (true) {
            System.out.println("""
                
                Reports Menu:
                1) Month To Date
                2) Previous Month
                3) Year To Date
                4) Previous Year
                5) Search by Vendor
                0) Back - go back to the Ledger page
                """);

            System.out.print("Choose: ");
            String choice = Scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    java.time.YearMonth thisMonth = java.time.YearMonth.now();
                    printList(filterByMonth(thisMonth));
                    break;
                case "2":
                    java.time.YearMonth prevMonth = java.time.YearMonth.now().minusMonths(1);
                    printList(filterByMonth(prevMonth));
                    break;
                case "3":
                    int thisYear = java.time.Year.now().getValue();
                    printList(filterByYear(thisYear));
                    break;
                case "4":
                    int lastYear = java.time.Year.now().minusYears(1).getValue();
                    printList(filterByYear(lastYear));
                    break;
                case "5":
                    System.out.print("Vendor name: ");
                    String vendor = Scanner.nextLine().trim().toLowerCase();
                    printList(filterByVendor(vendor));
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    public static ArrayList<data> filterByMonth(java.time.YearMonth ym) { // match month
        ArrayList<data> list = new ArrayList<>();
        for (data d : ALL) {
            java.time.YearMonth entryMonth = java.time.YearMonth.from(d.getDate());
            if (entryMonth.equals(ym)) list.add(d);
        }
        return list;
    }

    public static ArrayList<data> filterByYear(int year) { // match year
        ArrayList<data> list = new ArrayList<>();
        for (data d : ALL) {
            if (d.getDate().getYear() == year) list.add(d);
        }
        return list;
    }

    public static ArrayList<data> filterByVendor(String vendorLike) { // match vendor
        ArrayList<data> list = new ArrayList<>();
        for (data d : ALL) {
            if (d.getVendor().toLowerCase().contains(vendorLike)) list.add(d);
        }
        return list;
    }

    public static void printList(ArrayList<data> list) { // print table
        if (list.isEmpty()) {
            System.out.println("\n(no entries)\n");
            return;
        }
        list.sort(Comparator.comparing(data::getDate)
                .thenComparing(data::getTime)
                .reversed());
        System.out.println("\nDate       | Time     | Description          | Vendor            | Amount");
        System.out.println("-----------+----------+----------------------+-------------------+-----------");
        for (data d : list) {
            System.out.printf("%s | %-8s | %-20s | %-17s | %s%n",
                    d.getDate(), d.getTime(), d.getDescription(),
                    d.getVendor(), d.getAmount().toPlainString());
        }
        System.out.println();
    }
}
