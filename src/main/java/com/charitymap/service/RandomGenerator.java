package com.charitymap.service;

import com.charitymap.model.AidType;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.Point;

import java.util.Random;

/**
 * Generates entities at random positions, to satisfy the "mass add
 * with random positions" requirement of the specification.
 * <p>
 * Positions are drawn inside a rectangular area meant to represent the
 * city of Cergy. The bounds are kept simple on purpose so the team
 * fully controls them.
 *
 * @author CharityMap team
 */
public class RandomGenerator {

    /** Source of randomness. */
    private final Random random;

    /** The width of the generation area. */
    private final double width;

    /** The height of the generation area. */
    private final double height;

    /**
     * Builds a generator for a given area size.
     *
     * @param width  the width of the area (for example 1000)
     * @param height the height of the area (for example 1000)
     */
    public RandomGenerator(double width, double height) {
        this.random = new Random();
        this.width = width;
        this.height = height;
    }

    /**
     * Adds a given number of beneficiaries at random positions inside the specified
     * latitude/longitude bounding box.
     * <p>
     * For each beneficiary, the method chooses a random aid type, generates a
     * uniformly distributed point inside the provided bounds, and converts the
     * resulting geographic coordinates into the map's internal coordinate system
     * by applying the supplied projection function.
     *
     * @param map      the map that will receive the newly created beneficiaries
     * @param count    the number of beneficiaries to generate
     * @param minLng   the minimum longitude of the bounding box
     * @param maxLng   the maximum longitude of the bounding box
     * @param minLat   the minimum latitude of the bounding box
     * @param maxLat   the maximum latitude of the bounding box
     * @param projection a function used to convert the generated latitude/longitude
     *                   coordinates into a {@link Point} on the map
     */
    public void addRandomBeneficiaries(CharityMap map, int count,
            double minLng, double maxLng, double minLat, double maxLat,
            java.util.function.BiFunction<Double, Double, Point> projection) {
        AidType[] types = AidType.values();
        for (int i = 0; i < count; i++) {
            AidType need = types[random.nextInt(types.length)];
            double lng = minLng + random.nextDouble() * (maxLng - minLng);
            double lat = minLat + random.nextDouble() * (maxLat - minLat);
            Point p = projection.apply(lat, lng);
            map.addBeneficiary(new Beneficiary(p, need));
        }
    }
}