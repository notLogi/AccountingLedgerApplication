package com.pluralsight;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class UserInterface {
    private Scanner scanner;

    public UserInterface(){
        scanner = new Scanner(System.in);
    }


    public void runUI(){
        ArrayList<Transaction> transactions = new ArrayList<>();
        String FILE_NAME = "transactions.csv";

        String DATE_PATTERN = "yyyy-MM-dd";
        String TIME_PATTERN = "HH:mm:ss";

        DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
        DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);

        String DEFAULT = "\u001B[0m";
        String RED = "\u001B[31m";
        String GREEN = "\u001B[32m";
        String BLUE = "\u001B[34m";
        boolean running = true;

        while (running) {
            System.out.println(BLUE + "╔═══════════════════════════════════════════════╗");
            System.out.println(       "║  ┏━╸╻┏┓╻┏━┓┏┓╻┏━╸╻┏━┓╻  ╺┳╸┏━┓┏━┓┏━╸╻┏ ┏━╸┏━┓ ║\n" +
                    "║  ┣╸ ┃┃┗┫┣━┫┃┗┫┃  ┃┣━┫┃   ┃ ┣┳┛┣━┫┃  ┣┻┓┣╸ ┣┳┛ ║\n" +
                    "║  ╹  ╹╹ ╹╹ ╹╹ ╹┗━╸╹╹ ╹┗━╸ ╹ ╹┗╸╹ ╹┗━╸╹ ╹┗━╸╹┗  ║\n" +
                    "╚═══════════════════════════════════════════════╝" + DEFAULT );
            System.out.println("Please enter your username: ");
            System.out.println("If you do not have an account, please type R");
            String username = scanner.nextLine().trim();
            System.out.println("Please enter your password: ");
            String password = scanner.nextLine().trim();

            System.out.println("Choose An Option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }
}
