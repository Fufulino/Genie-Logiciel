package com.charitymap.cli;

import com.charitymap.geometry.VoronoiCell;
import com.charitymap.model.Association;
import com.charitymap.model.AidType;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;

import com.charitymap.model.Point;
import com.charitymap.service.CsvImporter;
import com.charitymap.service.MapIO;
import com.charitymap.service.RandomGenerator;

import java.util.List;
import java.util.Scanner;

/**
 * The command line version of the application.
 * <p>
 * This version adds the statistics view: once the geometry layer is
 * plugged in, each coverage zone exposes its social indicators
 * (covered beneficiaries, average travel distance, and local
 * coverage balance). Mass generation and persistence will follow.
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
        app.loadDemoData();
        app.run();
    }

    private void loadDemoData() {
        Association restos = new Association("Restos du Coeur", AidType.FOOD);
        Association croix = new Association("Croix Rouge", AidType.CARE);
        Association vestiaire = new Association("Vestiaire Solidaire", AidType.CLOTHING);

        map.addCenter(new DistributionCenter(new Point(200, 300), restos));
        map.addCenter(new DistributionCenter(new Point(600, 250), restos));
        map.addCenter(new DistributionCenter(new Point(400, 600), croix));
        map.addCenter(new DistributionCenter(new Point(750, 700), croix));
        map.addCenter(new DistributionCenter(new Point(300, 500), vestiaire));

        RandomGenerator generator = new RandomGenerator(1000, 1000);
        generator.addRandomBeneficiaries(map, 40);

        map.recompute();
    }

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
                System.out.println("Operation could not be completed: " + e.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    /**
     * Prints the list of available actions.
     */
    private void printMenu() {
        System.out.println();
        System.out.println("1. List distribution centers");
        System.out.println("2. List beneficiaries and their assignments");
        System.out.println("3. Display coverage zone statistics");
        System.out.println("4. Add a distribution center");
        System.out.println("5. Add random beneficiaries");
        System.out.println("6. Move a distribution center");
        System.out.println("7. Save map to binary file");
        System.out.println("8. Load map from binary file");
        System.out.println("9. Import centers from CSV");
        System.out.println("10. Delete a distribution center");
        System.out.println("11. Delete a beneficiary");
        System.out.println("12. Add a specific beneficiary");

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
                showStatistics();
                return true;
            case "4":
                addCenterInteractive();
                return true;
            case "5":
                addRandomBeneficiariesInteractive();
                return true;
            case "6":
                moveCenterInteractive();
                return true;
            case "7":
                saveInteractive();
                return true;
            case "8":
                loadInteractive();
                return true;
            case "9":
                importCsvInteractive();
                return true;
            case "10":
                removeCenterInteractive();
                return true;
            case "11":
                removeBeneficiaryInteractive();
                return true;
            case "12":
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
            System.out.println("There are no centers.");
            return;
        }
        for (DistributionCenter center : centers) {
            System.out.println("  " + center);
        }
    }

    /**
     * Prints every beneficiary and its assigned center.
     */
    private void listBeneficiaries() {
        List<Beneficiary> people = map.getBeneficiaries();
        if (people.isEmpty()) {
            System.out.println("There are no beneficiaries.");
            return;
        }
        for (Beneficiary b : people) {
            System.out.println("  " + b);
        }
    }

    /**
     * Prints the statistics report of every coverage zone, aid type
     * by aid type.
     */
    private void showStatistics() {
        for (AidType type : AidType.values()) {
            System.out.println();
            System.out.println("--- Aid Type: " + type.getLabel() + " ---");
            List<VoronoiCell> cells = map.getCells(type);
            if (cells.isEmpty()) {
                System.out.println("  No centers for this type.");
                continue;
            }
            for (VoronoiCell cell : cells) {
                System.out.println(cell.statisticsReport());
                System.out.println();
            }
        }
    }

    /**
     * Asks the user for the data needed to add a new center.
     */
    private void addCenterInteractive() {
        System.out.print("Association name: ");
        String name = scanner.nextLine().trim();
        AidType type = askAidType();
        double x = askCoordinate("Position X: ", 0, 1000);
        double y = askCoordinate("Position Y: ", 0, 1000);
        Association association = new Association(name, type);
        map.addCenter(new DistributionCenter(new Point(x, y), association));
        System.out.println("Center added and map recalculated.");
    }

    /**
     * Asks the user for the data needed to add a new beneficiary manually.
     */
    private void addBeneficiaryInteractive() {
        AidType type = askAidType();
        double x = askCoordinate("Position X: ", 0, 1000);
        double y = askCoordinate("Position Y: ", 0, 1000);
        map.addBeneficiary(new Beneficiary(new Point(x, y), type));
        System.out.println("Beneficiary added and map recalculated.");
    }

    /**
     * Asks the user for the data needed to add random beneficiaries.
     */
    private void addRandomBeneficiariesInteractive() {
        int count = (int) askDouble("Number of beneficiaries to generate: ");
        if (count <= 0) {
            System.out.println("The number must be positive.");
            return;
        }
        new RandomGenerator(1000, 1000).addRandomBeneficiaries(map, count);
        System.out.println(count + " beneficiaries added.");
    }

    /**
     * Asks the user for the data needed to move a center.
     */
    private void moveCenterInteractive() {
        listCenters();
        int id = (int) askDouble("ID of the center to move: ");
        DistributionCenter target = null;
        for (DistributionCenter c : map.getCenters()) {
            if (c.getId() == id) {
                target = c;
                break;
            }
        }
        if (target == null) {
            System.out.println("No center has this ID.");
            return;
        }
        double x = askCoordinate("New position X: ", 0, 1000);
        double y = askCoordinate("New position Y: ", 0, 1000);
        map.moveCenter(target, new Point(x, y));
        System.out.println("Center moved and map recalculated.");
    }

    /**
     * Asks the user for the path to save the map.
     */
    private void saveInteractive() {
        System.out.print("Backup file path: ");
        String path = scanner.nextLine().trim();
        try {
            new MapIO().save(map, path);
            System.out.println("Map saved.");
        } catch (Exception e) {
            System.out.println("Unable to save: " + e.getMessage());
        }
    }

    /**
     * Asks the user for the path to load the map from.
     */
    private void loadInteractive() {
        System.out.print("File path to load: ");
        String path = scanner.nextLine().trim();
        try {
            CharityMap loaded = new MapIO().load(path);
            map.clear();
            loaded.getCenters().forEach(map::addCenter);
            loaded.getBeneficiaries().forEach(map::addBeneficiary);
            System.out.println("Map loaded with "
                    + map.getCenters().size() + " centers and "
                    + map.getBeneficiaries().size()
                    + " beneficiaries.");
        } catch (Exception e) {
            System.out.println("Unable to load: " + e.getMessage());
        }
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
                System.out.println("Invalid aid type. Please type CLOTHING, FOOD, or CARE.");
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
            String raw = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Asks the user for a coordinate within a specific range.
     *
     * @param prompt the message shown to the user
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return a valid coordinate
     */
    private double askCoordinate(String prompt, double min, double max) {
        while (true) {
            double val = askDouble(prompt);
            if (val >= min && val <= max) {
                return val;
            }
            System.out.println("The coordinate must be between " + min + " and " + max + ".");
        }
    }

    /**
     * Asks the user for the path to the CSV file to import.
     */
    private void importCsvInteractive() {
        System.out.print("CSV file path: ");
        String path = scanner.nextLine().trim();
        try {
            int count = new CsvImporter().importCenters(map, path);
            System.out.println(count + " centers imported.");
        } catch (Exception e) {
            System.out.println("Unable to import: " + e.getMessage());
        }
    }

    /*
     * Asks the user for the ID of the center to remove.
     */
    private void removeCenterInteractive() {
        listCenters();
        List<DistributionCenter> centers = map.getCenters();
        if (centers.isEmpty()) {
            return;
        }
        int id = (int) askDouble("ID of the center to delete: ");
        DistributionCenter target = null;
        for (DistributionCenter c : centers) {
            if (c.getId() == id) {
                target = c;
                break;
            }
        }
        if (target == null) {
            System.out.println("No center has this ID.");
            return;
        }
        map.removeCenter(target);
        System.out.println("Center deleted and map recalculated.");
    }

    /**
     * Asks the user for the ID of the beneficiary to remove.
     */
    private void removeBeneficiaryInteractive() {
        listBeneficiaries();
        List<Beneficiary> people = map.getBeneficiaries();
        if (people.isEmpty()) {
            return;
        }
        int id = (int) askDouble("ID of the beneficiary to delete: ");
        Beneficiary target = null;
        for (Beneficiary b : people) {
            if (b.getId() == id) {
                target = b;
                break;
            }
        }
        if (target == null) {
            System.out.println("No beneficiary has this ID.");
            return;
        }
        map.removeBeneficiary(target);
        System.out.println("Beneficiary deleted.");
    }
}   