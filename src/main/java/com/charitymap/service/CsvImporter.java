package com.charitymap.service;

import com.charitymap.model.AidType;
import com.charitymap.model.Association;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Imports distribution centers from a CSV file.
 */
public class CsvImporter {

    /**
     * Creates a new CsvImporter instance.
     */
    public CsvImporter() {
        super();
    }

    /**
     * Imports centers from a CSV file into the given map using raw pixel coordinates.
     *
     * @param map  the map to add centers to
     * @param path the path to the CSV file
     * @return the number of centers successfully imported
     * @throws IOException if an error occurs while reading the file
     */
    public int importCenters(CharityMap map, String path) throws IOException {
        return importCenters(map, path, null);
    }

    /**
     * Imports centers from a CSV file into the given map, optionally projecting GPS coordinates.
     *
     * @param map        the map to add centers to
     * @param path       the path to the CSV file
     * @param projection a BiFunction to project latitude and longitude to pixels
     * @return the number of centers successfully imported
     * @throws IOException if an error occurs while reading the file
     */
    public int importCenters(CharityMap map, String path,
            java.util.function.BiFunction<Double, Double, Point> projection) throws IOException {
        Map<String, Association> knownAssociations = new HashMap<>();
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                try {
                    String[] parts = line.split("[,;]");
                    if (parts.length < 4) {
                        continue;
                    }
                    double val1 = Double.parseDouble(parts[0].trim());
                    double val2 = Double.parseDouble(parts[1].trim());
                    String name = parts[2].trim();
                    AidType type = AidType.valueOf(parts[3].trim().toUpperCase());

                    Association association = knownAssociations.computeIfAbsent(
                            name + "_" + type.name(),
                            k -> new Association(name, type));

                    Point p;
                    // Auto-detect if coordinates are GPS (e.g. lat ~ 49.0, lon ~ 2.0)
                    if (projection != null && val1 >= 48.0 && val1 <= 50.0 && val2 >= 1.0 && val2 <= 3.0) {
                        p = projection.apply(val1, val2);
                    } else {
                        p = new Point(val1, val2);
                    }

                    map.addCenter(new DistributionCenter(p, association));
                    count++;
                } catch (Exception e) {
                    // Ignore malformed lines
                }
            }
        }
        return count;
    }
}
