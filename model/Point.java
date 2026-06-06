package com.cergy.charitymap.model;

import java.io.Serializable;

/**
 * An immutable 2D point with double precision coordinates.
 * <p>
 * This is the most fundamental building block of the whole application.
 * It is intentionally immutable: once created, its coordinates never
 * change. This avoids a whole class of subtle bugs when points are
 * shared between sites, beneficiaries, triangles and Voronoi cells.
 *
 * @author CharityMap team
 */
public final class Point implements Serializable {

    /** Serialization version identifier (required for stable binary export). */
    private static final long serialVersionUID = 1L;

    /** The horizontal coordinate. */
    private final double x;

    /** The vertical coordinate. */
    private final double y;

    /**
     * Builds a new point.
     *
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the horizontal coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the vertical coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Computes the Euclidean distance between this point and another point.
     *
     * @param other the other point (must not be null)
     * @return the straight line distance between the two points
     */
    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Computes the squared Euclidean distance to another point.
     * <p>
     * This avoids the cost of a square root and is enough when we only
     * need to compare distances (for example to find the closest center).
     *
     * @param other the other point (must not be null)
     * @return the squared distance between the two points
     */
    public double squaredDistanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    /**
     * Two points are considered equal when their coordinates are very
     * close. We use a small tolerance because floating point arithmetic
     * is never exact.
     *
     * @param obj the object to compare with
     * @return true when both points represent the same location
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point) obj;
        double epsilon = 1e-9;
        return Math.abs(this.x - other.x) < epsilon
                && Math.abs(this.y - other.y) < epsilon;
    }

    /**
     * Hash code consistent with {@link #equals(Object)}.
     * <p>
     * Coordinates are rounded before hashing so that two points
     * considered equal also share the same hash code.
     *
     * @return a hash code for this point
     */
    @Override
    public int hashCode() {
        long rx = Math.round(this.x * 1e6);
        long ry = Math.round(this.y * 1e6);
        return Long.hashCode(rx * 31 + ry);
    }

    /**
     * Returns a readable text representation, useful for the command
     * line version and for debugging.
     *
     * @return a string such as "(12.50, 8.00)"
     */
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
