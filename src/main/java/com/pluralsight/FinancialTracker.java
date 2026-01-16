package com.pluralsight;

import com.pluralsight.configurations.DatabaseConfig;
import com.pluralsight.data.TransactionDao;
import com.pluralsight.data.UserDao;
import com.pluralsight.models.Transaction;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);

    public static final String DEFAULT = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String YELLOW = "\u001B[33m";

    // DAOs
    private static UserDao userDAO;
    private static TransactionDao transactionDao;

    // Current logged-in user
    private static Integer currentUserId = null;
    private static String currentUsername = null;

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        // Initialize database connection and DAOs
        DataSource dataSource = DatabaseConfig.getDataSource();
        UserDao userDao = new UserDao(dataSource);
        transactionDao = new TransactionDao(dataSource);

        Scanner scanner = new Scanner(System.in);

        // User login/registration
        if (!loginOrRegister(scanner, userDao)) {
            System.out.println("Exiting application...");
            scanner.close();
            return;
        }

        boolean run = true;
        while (run) {
            System.out.println();
            System.out.println(BLUE + "╔═══════════════════════════════════════════════╗");
            System.out.println(       "║  ┏━╸╻┏┓╻┏━┓┏┓╻┏━╸╻┏━┓╻  ╺┳╸┏━┓┏━┓┏━╸╻┏ ┏━╸┏━┓ ║\n" +
                    "║  ┣╸ ┃┃┗┫┣━┫┃┗┫┃  ┃┣━┫┃   ┃ ┣┳┛┣━┫┃  ┣┻┓┣╸ ┣┳┛ ║\n" +
                    "║  ╹  ╹╹ ╹╹ ╹╹ ╹┗━╸╹╹ ╹┗━╸ ╹ ╹┗╸╹ ╹┗━╸╹ ╹┗━╸╹┗  ║\n" +
                    "╚═══════════════════════════════════════════════╝" + DEFAULT);
            System.out.println(YELLOW + "Logged in as: " + currentUsername + DEFAULT);
            System.out.println("Choose An Option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("U) Switch User");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "U" -> {
                    if (loginOrRegister(scanner, userDao)) {
                        System.out.println(GREEN + "User switched successfully!" + DEFAULT);
                    }
                }
                case "X" -> run = false;
                default -> System.out.println(RED + "INVALID OPTION" + DEFAULT);
            }
        }
        scanner.close();
        System.out.println("Thank you for using Financial Tracker!");
    }

    /* ------------------------------------------------------------------
       User login/registration
       ------------------------------------------------------------------ */
    private static boolean loginOrRegister(Scanner scanner, UserDao userDao) {
        System.out.println();
        System.out.println(GREEN + "═══ USER LOGIN / REGISTRATION ═══" + DEFAULT);
        System.out.println("1) Login");
        System.out.println("2) Register New User");
        System.out.println("3) View All Users");
        System.out.println("0) Exit");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                return login(scanner, userDao);
            case "2":
                return register(scanner, userDao);
            case "3":
                viewAllUsers(userDao);
                return loginOrRegister(scanner, userDao);
            case "0":
                return false;
            default:
                System.out.println(RED + "Invalid option" + DEFAULT);
                return loginOrRegister(scanner, userDao);
        }
    }

    private static boolean login(Scanner scanner, UserDao userDao) {
        System.out.println("Enter username:");
        String username = scanner.nextLine().trim();

        Integer userId = userDao.login(username);

        if (userId != null) {
            currentUserId = userId;
            currentUsername = username;
            System.out.println(GREEN + "Login successful! Welcome, " + username + "!" + DEFAULT);
            return true;
        } else {
            System.out.println(RED + "User not found. Please try again or register." + DEFAULT);
            return loginOrRegister(scanner, userDao);
        }
    }

    private static boolean register(Scanner scanner, UserDao userDao) {
        System.out.println("Enter new username:");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println(RED + "Username cannot be empty!" + DEFAULT);
            return register(scanner, userDao);
        }

        Integer userId = userDao.register(username);

        if (userId != null) {
            currentUserId = userId;
            currentUsername = username;
            System.out.println(GREEN + "Registration successful! Welcome, " + username + "!" + DEFAULT);
            return true;
        } else {
            System.out.println(RED + "Registration failed. Username may already exist." + DEFAULT);
            return loginOrRegister(scanner, userDao);
        }
    }

    private static void viewAllUsers(UserDao userDao) {
        System.out.println();
        System.out.println(GREEN + "═══ ALL USERS ═══" + DEFAULT);
        List<String> usernames = userDao.getAllUsernames();

        if (usernames.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("Registered Users:");
            System.out.println("------------------");
            for (String username : usernames) {
                System.out.println("- " + username);
            }
        }
        System.out.println();
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    private static void addDeposit(Scanner scanner) {
        LocalDate dateFormatted = null;
        LocalTime timeFormatted = null;
        boolean validDate = false;

        while (!validDate) {
            try {
                System.out.println("Enter Date (yyyy-MM-dd):");
                String date = scanner.nextLine();
                dateFormatted = LocalDate.parse(date, DATE_FMT);
                validDate = true;
            } catch (Exception e) {
                System.out.println(RED + "INVALID DATE" + DEFAULT);
            }
        }

        boolean validTime = false;
        while (!validTime) {
            try {
                System.out.println("Enter Time (HH:mm:ss):");
                String time = scanner.nextLine();
                timeFormatted = LocalTime.parse(time, TIME_FMT);
                validTime = true;
            } catch (Exception e) {
                System.out.println(RED + "Invalid Time. Use Format (HH:mm:ss)" + DEFAULT);
            }
        }

        System.out.println("Enter Description:");
        String description = scanner.nextLine();

        System.out.println("Enter Vendor:");
        String vendor = scanner.nextLine();

        double positiveAmount = 0.0;
        boolean validAmount = false;

        while (!validAmount) {
            try {
                System.out.println("Enter Amount:");
                positiveAmount = Double.parseDouble(scanner.nextLine());
                if (positiveAmount <= 0) {
                    System.out.println(RED + "Invalid number. Enter Positive Number" + DEFAULT);
                } else {
                    validAmount = true;
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid Input. Please Enter A Numeric Value" + DEFAULT);
            }
        }

        Transaction transaction = new Transaction(
                dateFormatted,
                timeFormatted,
                description,
                vendor,
                positiveAmount,
                currentUserId
        );

        Transaction created = transactionDao.createTransaction(transaction);

        if (created != null) {
            System.out.println(GREEN + "Deposit Saved!" + DEFAULT);
        } else {
            System.out.println(RED + "Failed to save deposit." + DEFAULT);
        }
    }

    private static void addPayment(Scanner scanner) {
        System.out.println("FILL ALL VALUES!");
        LocalDate dateFormatted = null;
        LocalTime timeFormatted = null;
        boolean validDate = false;

        while (!validDate) {
            try {
                System.out.println("Enter Date (yyyy-MM-dd):");
                String date = scanner.nextLine();
                dateFormatted = LocalDate.parse(date, DATE_FMT);
                validDate = true;
            } catch (Exception e) {
                System.out.println(RED + "Invalid Date. Use Format (yyyy-MM-dd)" + DEFAULT);
            }
        }

        boolean validTime = false;
        while (!validTime) {
            try {
                System.out.println("Enter Time (HH:mm:ss):");
                String time = scanner.nextLine();
                timeFormatted = LocalTime.parse(time, TIME_FMT);
                validTime = true;
            } catch (Exception e) {
                System.out.println(RED + "Invalid Time. Use Format (HH:mm:ss)" + DEFAULT);
            }
        }

        System.out.println("Enter Description:");
        String description = scanner.nextLine();

        System.out.println("Enter Vendor:");
        String vendor = scanner.nextLine();

        double amount = 0;
        boolean goodAmount = false;

        while (!goodAmount) {
            try {
                System.out.println("Enter Amount (greater than 0):");
                amount = Double.parseDouble(scanner.nextLine());
                if (amount <= 0) {
                    System.out.println(RED + "Invalid Number. Enter Positive." + DEFAULT);
                } else {
                    goodAmount = true;
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid Input. Please Enter A Numeric Value" + DEFAULT);
            }
        }

        double negativeAmount = -Math.abs(amount);

        Transaction transaction = new Transaction(
                dateFormatted,
                timeFormatted,
                description,
                vendor,
                negativeAmount,
                currentUserId
        );

        Transaction created = transactionDao.createTransaction(transaction);

        if (created != null) {
            System.out.println(GREEN + "Payment Recorded!" + DEFAULT);
        } else {
            System.out.println(RED + "Failed to record payment." + DEFAULT);
        }
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println(GREEN + "| LEDGER MENU |" + DEFAULT);
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println(RED + "Invalid option" + DEFAULT);
            }
        }
    }

    private static void displayLedger() {
        System.out.println();
        System.out.println(GREEN + "| ALL TRANSACTIONS |" + DEFAULT);
        List<Transaction> transactions = transactionDao.getTransactionsByUserId(currentUserId);
        displayTransactions(transactions);
    }

    private static void displayDeposits() {
        System.out.println();
        System.out.println(GREEN + "| DEPOSITS |" + DEFAULT);
        List<Transaction> deposits = transactionDao.getDepositsByUserId(currentUserId);
        displayTransactions(deposits);
    }

    private static void displayPayments() {
        System.out.println();
        System.out.println(GREEN + "| PAYMENTS |" + DEFAULT);
        List<Transaction> payments = transactionDao.getPaymentsByUserId(currentUserId);
        displayTransactions(payments);
    }

    private static void displayTransactions(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("Date----------Time---------Description--------------------Vendor--------------Amount");
        System.out.println("=======================================================================================");

        for (Transaction transaction : transactions) {
            System.out.printf("%-12s %-10s %-30s %-20s %10.2f\n",
                    transaction.getDate(),
                    transaction.getTime(),
                    transaction.getDescription(),
                    transaction.getVendor(),
                    transaction.getAmount());
        }
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println(GREEN + "| REPORTS MENU |" + DEFAULT);
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    LocalDate start = LocalDate.now().withDayOfMonth(1);
                    LocalDate end = LocalDate.now();
                    filterTransactionsByDate(start, end);
                }
                case "2" -> {
                    LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                    filterTransactionsByDate(start, end);
                }
                case "3" -> {
                    LocalDate start = LocalDate.now().withDayOfYear(1);
                    LocalDate end = LocalDate.now();
                    filterTransactionsByDate(start, end);
                }
                case "4" -> {
                    LocalDate start = LocalDate.now().minusYears(1).withDayOfYear(1);
                    LocalDate end = start.withDayOfYear(start.lengthOfYear());
                    filterTransactionsByDate(start, end);
                }
                case "5" -> {
                    System.out.println("Enter Vendor:");
                    String vendor = scanner.nextLine();
                    filterTransactionsByVendor(vendor);
                }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println(RED + "Invalid Option" + DEFAULT);
            }
        }
    }

    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        System.out.println();
        System.out.println(GREEN + "| TRANSACTIONS BY DATE |" + DEFAULT);
        List<Transaction> transactions = transactionDao.getTransactionsByDateRange(currentUserId, start, end);
        displayTransactions(transactions);
    }

    private static void filterTransactionsByVendor(String vendor) {
        System.out.println();
        System.out.println(GREEN + "| TRANSACTIONS BY VENDOR |" + DEFAULT);
        List<Transaction> transactions = transactionDao.getTransactionsByVendor(currentUserId, vendor);
        displayTransactions(transactions);
    }

    private static void customSearch(Scanner scanner) {
        System.out.println();
        System.out.println(GREEN + "| CUSTOM-SEARCH MENU |" + DEFAULT);

        System.out.println("Start date (yyyy-MM-dd, Leave Empty for None):");
        String stringStartDate = scanner.nextLine().trim();

        System.out.println("Enter End date (yyyy-MM-dd, Leave Empty for None):");
        String stringEndDate = scanner.nextLine().trim();

        System.out.println("Description (Leave Empty for None):");
        String description = scanner.nextLine();

        System.out.println("Vendor (Leave Empty for None):");
        String vendor = scanner.nextLine();

        System.out.println("Amount (Leave Empty for None):");
        String amount = scanner.nextLine();

        // Get all transactions for the user
        List<Transaction> allTransactions = transactionDao.getTransactionsByUserId(currentUserId);
        List<Transaction> filteredTransactions = new java.util.ArrayList<>();

        Double finalAmount = null;
        if (!amount.isEmpty()) {
            try {
                finalAmount = Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid amount format" + DEFAULT);
                return;
            }
        }

        LocalDate startDate = null;
        if (!stringStartDate.isEmpty()) {
            try {
                startDate = LocalDate.parse(stringStartDate);
            } catch (Exception e) {
                System.out.println(RED + "Invalid start date format" + DEFAULT);
                return;
            }
        }

        LocalDate endDate = null;
        if (!stringEndDate.isEmpty()) {
            try {
                endDate = LocalDate.parse(stringEndDate);
            } catch (Exception e) {
                System.out.println(RED + "Invalid end date format" + DEFAULT);
                return;
            }
        }

        // Filter transactions
        for (Transaction transaction : allTransactions) {
            if (startDate != null && transaction.getDate().isBefore(startDate)) continue;
            if (endDate != null && transaction.getDate().isAfter(endDate)) continue;
            if (!description.isEmpty() && !description.equalsIgnoreCase(transaction.getDescription())) continue;
            if (!vendor.isEmpty() && !vendor.equalsIgnoreCase(transaction.getVendor())) continue;
            if (finalAmount != null && transaction.getAmount() != finalAmount) continue;

            filteredTransactions.add(transaction);
        }

        System.out.println();
        System.out.println(GREEN + "| CUSTOM SEARCH RESULTS |" + DEFAULT);
        displayTransactions(filteredTransactions);
    }
}