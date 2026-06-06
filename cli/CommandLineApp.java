package com.cergy.charitymap.cli;

import com.cergy.charitymap.geometry.VoronoiCell;
import com.cergy.charitymap.model.Association;
import com.cergy.charitymap.model.AidType;
import com.cergy.charitymap.model.Beneficiary;
import com.cergy.charitymap.model.CharityMap;
import com.cergy.charitymap.model.DistributionCenter;
import com.cergy.charitymap.model.Distributor;
import com.cergy.charitymap.model.Point;
import com.cergy.charitymap.service.MapIO;
import com.cergy.charitymap.service.RandomGenerator;

import java.util.List;
import java.util.Scanner;

/**
 * The command line version of the application.
 * <p>
 * This version adds the statistics view: once the geometry layer is
 * plugged in, each coverage zone exposes its social indicators
 * (covered beneficiaries, average travel distance, load per
 * distributor). Mass generation and persistence will follow.
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

    /**
     * Fills the map with a small demonstration dataset so the user can
     * see something meaningful immediately.
     */
    private void loadDemoData() {
        Association restos = new Association("Restos du Coeur", AidType.FOOD);
        Association croix = new Association("Croix Rouge", AidType.CARE);
        Association vestiaire =
                new Association("Vestiaire Solidaire", AidType.CLOTHING);

        map.addCenter(new DistributionCenter(new Point(200, 300), restos));
        map.addCenter(new DistributionCenter(new Point(600, 250), restos));
        map.addCenter(new DistributionCenter(new Point(400, 600), croix));
        map.addCenter(new DistributionCenter(new Point(750, 700), croix));
        map.addCenter(new DistributionCenter(
                new Point(300, 500), vestiaire));

        RandomGenerator generator = new RandomGenerator(1000, 1000);
        generator.addRandomBeneficiaries(map, 40);
        generator.addRandomDistributors(map, 12);
        map.recompute();
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
        System.out.println("3. Show statistics per coverage zone");
        System.out.println("4. Add a center");
        System.out.println("5. Add random beneficiaries");
        System.out.println("6. Move a center");
        System.out.println("7. Save the map to a binary file");
        System.out.println("8. Load a map from a binary file");
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
     * Prints every beneficiary and its assigned center.
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
     * Prints the statistics report of every coverage zone, aid type
     * by aid type.
     */
    private void showStatistics() {
        for (AidType type : AidType.values()) {
            System.out.println();
            System.out.println("--- Aid type: " + type.getLabel() + " ---");
            List<VoronoiCell> cells = map.getCells(type);
            if (cells.isEmpty()) {
                System.out.println("  No center of this type.");
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
        double x = askDouble("X position: ");
        double y = askDouble("Y position: ");
        Association association = new Association(name, type);
        map.addCenter(new DistributionCenter(new Point(x, y), association));
        System.out.println("Center added and map recomputed.");
    }

    /**
     * Asks the user how many random beneficiaries to add.
     */
    private void addRandomBeneficiariesInteractive() {
        int count = (int) askDouble("How many beneficiaries: ");
        if (count <= 0) {
            System.out.println("The number must be positive.");
            return;
        }
        new RandomGenerator(1000, 1000)
                .addRandomBeneficiaries(map, count);
        System.out.println(count + " beneficiaries added.");
    }

    /**
     * Asks the user which center to move and where.
     */
    private void moveCenterInteractive() {
        listCenters();
        int id = (int) askDouble("Id of the center to move: ");
        DistributionCenter target = null;
        for (DistributionCenter c : map.getCenters()) {
            if (c.getId() == id) {
                target = c;
                break;
            }
        }
        if (target == null) {
            System.out.println("No center has this id.");
            return;
        }
        double x = askDouble("New X position: ");
        double y = askDouble("New Y position: ");
        map.moveCenter(target, new Point(x, y));
        System.out.println("Center moved and map recomputed.");
    }

    /**
     * Asks the user for a file path then saves the map.
     */
    private void saveInteractive() {
        System.out.print("File path to save to: ");
        String path = scanner.nextLine().trim();
        try {
            new MapIO().save(map, path);
            System.out.println("Map saved.");
        } catch (Exception e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    /**
     * Asks the user for a file path then loads a map from it.
     */
    private void loadInteractive() {
        System.out.print("File path to load from: ");
        String path = scanner.nextLine().trim();
        try {
            CharityMap loaded = new MapIO().load(path);
            System.out.println("Map loaded with "
                    + loaded.getCenters().size() + " centers and "
                    + loaded.getBeneficiaries().size()
                    + " beneficiaries.");
        } catch (Exception e) {
            System.out.println("Could not load: " + e.getMessage());
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
