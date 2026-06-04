package com.cergy.charitymap.service;

import java.util.Random;

import com.cergy.charitymap.model.Point;

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
}
