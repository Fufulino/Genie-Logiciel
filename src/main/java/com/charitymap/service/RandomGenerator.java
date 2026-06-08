package com.charitymap.service;

import com.charitymap.model.AidType;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Distributor;
import com.charitymap.model.Point;

import java.util.List;
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
     * Draws one random position inside the generation area.
     *
     * @return a random point
     */
    private Point randomPoint() {
        return new Point(random.nextDouble() * width,
                random.nextDouble() * height);
    }

    /**
     * Adds a given number of beneficiaries at random positions, each
     * with a random aid need.
     *
     * @param map   the map to fill
     * @param count how many beneficiaries to create
     */
    public void addRandomBeneficiaries(CharityMap map, int count) {
        AidType[] types = AidType.values();
        for (int i = 0; i < count; i++) {
            AidType need = types[random.nextInt(types.length)];
            map.addBeneficiary(new Beneficiary(randomPoint(), need));
        }
    }

    /**
     * Adds a given number of distributors at random positions, each
     * attached to a randomly chosen existing center.
     *
     * @param map   the map containing the centers
     * @param count how many distributors to create
     */
    public void addRandomDistributors(CharityMap map, int count) {
        List<DistributionCenter> existing = map.getCenters();
        if (existing.isEmpty()) {
            return;
        }
        for (int i = 0; i < count; i++) {
            DistributionCenter home =
                    existing.get(random.nextInt(existing.size()));
            map.addDistributor(new Distributor(randomPoint(), home));
        }
    }
}

