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
                System.out.println("L'opération n'a pas pu être finalisée : " + e.getMessage());
            }
        }
        System.out.println("Au revoir.");
    }

    /**
     * Prints the list of available actions.
     */
    private void printMenu() {
        System.out.println();
        System.out.println("1. Lister les centres de distribution");
        System.out.println("2. Lister les bénéficiaires et leurs affectations");
        System.out.println("3. Afficher les statistiques par zone de couverture");
        System.out.println("4. Ajouter un centre de distribution");
        System.out.println("5. Ajouter des bénéficiaires aléatoires");
        System.out.println("6. Déplacer un centre de distribution");
        System.out.println("7. Sauvegarder la carte dans un fichier binaire");
        System.out.println("8. Charger une carte depuis un fichier binaire");
        System.out.println("9. Importer des centres depuis un CSV");
        System.out.println("10. Supprimer un centre de distribution");
        System.out.println("11. Supprimer un bénéficiaire");

        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
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

            case "0":
                return false;
            default:
                System.out.println("Choix inconnu, veuillez réessayer.");
                return true;
        }
    }

    /**
     * Prints every center.
     */
    private void listCenters() {
        List<DistributionCenter> centers = map.getCenters();
        if (centers.isEmpty()) {
            System.out.println("Il n'y a aucun centre.");
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
            System.out.println("Il n'y a aucun bénéficiaire.");
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
            System.out.println("--- Type d'aide : " + type.getLabel() + " ---");
            List<VoronoiCell> cells = map.getCells(type);
            if (cells.isEmpty()) {
                System.out.println("  Aucun centre pour ce type.");
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
        System.out.print("Nom de l'association : ");
        String name = scanner.nextLine().trim();
        AidType type = askAidType();
        double x = askDouble("Position X : ");
        double y = askDouble("Position Y : ");
        Association association = new Association(name, type);
        map.addCenter(new DistributionCenter(new Point(x, y), association));
        System.out.println("Centre ajouté et carte recalculée.");
    }

    /**
     * Asks the user for the data needed to add random beneficiaries.
     */
    private void addRandomBeneficiariesInteractive() {
        int count = (int) askDouble("Nombre de bénéficiaires à générer : ");
        if (count <= 0) {
            System.out.println("Le nombre doit être positif.");
            return;
        }
        new RandomGenerator(1000, 1000).addRandomBeneficiaries(map, count);
        System.out.println(count + " bénéficiaires ajoutés.");
    }

    /**
     * Asks the user for the data needed to move a center.
     */
    private void moveCenterInteractive() {
        listCenters();
        int id = (int) askDouble("ID du centre à déplacer : ");
        DistributionCenter target = null;
        for (DistributionCenter c : map.getCenters()) {
            if (c.getId() == id) {
                target = c;
                break;
            }
        }
        if (target == null) {
            System.out.println("Aucun centre n'a cet ID.");
            return;
        }
        double x = askDouble("Nouvelle position X : ");
        double y = askDouble("Nouvelle position Y : ");
        map.moveCenter(target, new Point(x, y));
        System.out.println("Centre déplacé et carte recalculée.");
    }

    /**
     * Asks the user for the path to save the map.
     */
    private void saveInteractive() {
        System.out.print("Chemin du fichier de sauvegarde : ");
        String path = scanner.nextLine().trim();
        try {
            new MapIO().save(map, path);
            System.out.println("Carte sauvegardée.");
        } catch (Exception e) {
            System.out.println("Impossible de sauvegarder : " + e.getMessage());
        }
    }

    /**
     * Asks the user for the path to load the map from.
     */
    private void loadInteractive() {
        System.out.print("Chemin du fichier à charger : ");
        String path = scanner.nextLine().trim();
        try {
            CharityMap loaded = new MapIO().load(path);
            map.clear();
            loaded.getCenters().forEach(map::addCenter);
            loaded.getBeneficiaries().forEach(map::addBeneficiary);
            System.out.println("Carte chargée avec "
                    + map.getCenters().size() + " centres et "
                    + map.getBeneficiaries().size()
                    + " bénéficiaires.");
        } catch (Exception e) {
            System.out.println("Impossible de charger : " + e.getMessage());
        }
    }

    /**
     * Reads a valid aid type from the user, asking again on bad input.
     *
     * @return the chosen aid type
     */
    private AidType askAidType() {
        while (true) {
            System.out.print("Type d'aide (CLOTHING, FOOD, CARE) : ");
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Flux d'entrée fermé.");
            }
            String raw = scanner.nextLine().trim().toUpperCase();
            try {
                return AidType.valueOf(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Type inconnu, veuillez réessayer.");
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
                throw new IllegalStateException("Flux d'entrée fermé.");
            }
            String raw = scanner.nextLine().trim();
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            }
        }
    }

    /**
     * Asks the user for the path to the CSV file to import.
     */
    private void importCsvInteractive() {
        System.out.print("Chemin du fichier CSV : ");
        String path = scanner.nextLine().trim();
        try {
            int count = new CsvImporter().importCenters(map, path);
            System.out.println(count + " centres importés.");
        } catch (Exception e) {
            System.out.println("Impossible d'importer : " + e.getMessage());
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
        int id = (int) askDouble("ID du centre à supprimer : ");
        DistributionCenter target = null;
        for (DistributionCenter c : centers) {
            if (c.getId() == id) {
                target = c;
                break;
            }
        }
        if (target == null) {
            System.out.println("Aucun centre n'a cet ID.");
            return;
        }
        map.removeCenter(target);
        System.out.println("Centre supprimé et carte recalculée.");
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
        int id = (int) askDouble("ID du bénéficiaire à supprimer : ");
        Beneficiary target = null;
        for (Beneficiary b : people) {
            if (b.getId() == id) {
                target = b;
                break;
            }
        }
        if (target == null) {
            System.out.println("Aucun bénéficiaire n'a cet ID.");
            return;
        }
        map.removeBeneficiary(target);
        System.out.println("Bénéficiaire supprimé.");
    }


}
