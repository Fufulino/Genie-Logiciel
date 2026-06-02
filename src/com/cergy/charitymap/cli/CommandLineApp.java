package com.cergy.charitymap.cli;

import com.cergy.charitymap.model.AidType;
import com.cergy.charitymap.model.Association;
import com.cergy.charitymap.model.Beneficiary;
import com.cergy.charitymap.model.CharityMap;
import com.cergy.charitymap.model.DistributionCenter;
import com.cergy.charitymap.model.Point;

import java.util.List;
import java.util.Scanner;

/**
 * The command line version of the application.
 * <p>
 * This first version offers the basic listing and adding operations.
 * The interactive map will grow with the project: dynamic statistics,
 * mass generation, save and load will be added in further commits.
 *
 * @author CharityMap team
 */
public class CommandLineApp {

    /** The map being manipulated. */
    private final CharityMap map;

    /** Reader for the user input. */
    private final Scanner scanner;

    /**
     * Builds the command line application with an empty map.
     */
    public CommandLineApp() {
        this.map = new CharityMap();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Program entry point.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        CommandLineApp app = new CommandLineApp();
        app.run();
    }

    /**
     * Runs the main interactive loop until the user quits.
     */
    private void run() {
        System.out.println("=== CharityMap - Cergy associations ===");
        boolean running = true;
        while (running) {
            printMenu();
            if (!scanner.hasNextLine()) {
                System.out.println();
                break;
            }
            String choice = scanner.nextLine().trim();
            try {
                running = handleChoice(choice);
            } catch (Exception e) {
                System.out.println(
                        "The operation could not be completed: "
                                + e.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    /**
     * Prints the list of available actions.
     */
    private void printMenu() {
        System.out.println();
        System.out.println("1. List centers");
        System.out.println("2. List beneficiaries");
        System.out.println("3. Add a center");
        System.out.println("4. Add a beneficiary");
        System.out.println("0. Quit");
        System.out.print("Your choice: ");
    }

    /**
     * Dispatches one menu choice.
     *
     * @param choice the raw text typed by the user
     * @return false when the program must stop, true otherwise
     */
    private boolean handleChoice(String choice) {
        switch (choice) {
            case "1":
                listCenters();
                return true;
            case "2":
                listBeneficiaries();
                return true;
            case "3":
                addCenterInteractive();
                return true;
            case "4":
                addBeneficiaryInteractive();
                return true;
            case "0":
                return false;
            default:
                System.out.println("Unknown choice, please try again.");
                return true;
        }
    }

    /**
     * Prints every center.
     */
    private void listCenters() {
        List<DistributionCenter> centers = map.getCenters();
        if (centers.isEmpty()) {
            System.out.println("There is no center yet.");
            return;
        }
        for (DistributionCenter center : centers) {
            System.out.println("  " + center);
        }
    }

    /**
     * Prints every beneficiary.
     */
    private void listBeneficiaries() {
        List<Beneficiary> people = map.getBeneficiaries();
        if (people.isEmpty()) {
            System.out.println("There is no beneficiary yet.");
            return;
        }
        for (Beneficiary b : people) {
            System.out.println("  " + b);
        }
    }

    /**
     * Asks the user for the data needed to add a new center.
     */
    private void addCenterInteractive() {
        System.out.print("Association name: ");
        String name = scanner.nextLine().trim();
        AidType type = askAidType();
        double x = askDouble("X position: ");
        double y = askDouble("Y position: ");
        Association association = new Association(name, type);
        map.addCenter(new DistributionCenter(new Point(x, y), association));
        System.out.println("Center added.");
    }

    /**
     * Asks the user for the data needed to add a new beneficiary.
     */
    private void addBeneficiaryInteractive() {
        AidType need = askAidType();
        double x = askDouble("X position: ");
        double y = askDouble("Y position: ");
        map.addBeneficiary(new Beneficiary(new Point(x, y), need));
        System.out.println("Beneficiary added.");
    }

    /**
     * Reads a valid aid type from the user, asking again on bad input.
     *
     * @return the chosen aid type
     */
    private AidType askAidType() {
        while (true) {
            System.out.print("Aid type (CLOTHING, FOOD, CARE): ");
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Input stream closed.");
            }
            String raw = scanner.nextLine().trim().toUpperCase();
            try {
                return AidType.valueOf(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown aid type, please try again.");
            }
        }
    }

    /**
     * Reads a valid number from the user, asking again on bad input.
     *
     * @param prompt the message shown to the user
     * @return the number typed by the user
     */
    private double askDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Input stream closed.");
            }
            String raw = scanner.nextLine().trim();
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                System.out.println("Please type a valid number.");
            }
        }
    }
}