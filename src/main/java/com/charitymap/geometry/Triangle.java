package com.charitymap.geometry;

import com.charitymap.model.Point;

import java.io.Serializable;

/**
 * A triangle defined by three points, used by the Delaunay
 * triangulation.
 * <p>
 * The key operation here is {@link #circumcircleContains(Point)}: the
 * Bowyer-Watson algorithm repeatedly asks "does the circumscribed
 * circle of this triangle contain the new point?". A triangle also
 * exposes its circumcenter, which is a Voronoi vertex by duality.
 *
 * @author CharityMap team
 */
public class Triangle implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** The first vertex. */
    private final Point a;

    /** The second vertex. */
    private final Point b;

    /** The third vertex. */
    private final Point c;

    /**
     * Builds a triangle from three points.
     *
     * @param a the first vertex (must not be null)
     * @param b the second vertex (must not be null)
     * @param c the third vertex (must not be null)
     */
    public Triangle(Point a, Point b, Point c) {
        if (a == null || b == null || c == null) {
            throw new IllegalArgumentException(
                    "A triangle needs three non null vertices.");
        }
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Returns the first vertex.
     *
     * @return vertex a
     */
    public Point getA() {
        return a;
    }

    /**
     * Returns the second vertex.
     *
     * @return vertex b
     */
    public Point getB() {
        return b;
    }

    /**
     * Returns the third vertex.
     *
     * @return vertex c
     */
    public Point getC() {
        return c;
    }

    /**
     * Returns the circumcenter of this triangle, that is to say a
     * Voronoi vertex.
     *
     * @return the circumcenter, or null when the vertices are aligned
     */
    public Point getCircumcenter() {
        return GeometryUtils.circumcenter(a, b, c);
    }

    /**
     * Tells whether a given point lies strictly inside the circle
     * passing through the three vertices of this triangle. This is the
     * core test of the Bowyer-Watson algorithm.
     *
     * @param p the point to test
     * @return true when the point is inside the circumscribed circle
     */
    public boolean circumcircleContains(Point p) {
        Point center = getCircumcenter();
        if (center == null) {
            // Degenerate triangle: treat it as containing nothing.
            return false;
        }
        double radiusSquared = center.squaredDistanceTo(a);
        double distanceSquared = center.squaredDistanceTo(p);
        return distanceSquared < radiusSquared - 1e-9;
    }

    /**
     * Tells whether this triangle uses the given point as one of its
     * three vertices.
     *
     * @param p the point to look for
     * @return true when p is a vertex of this triangle
     */
    public boolean hasVertex(Point p) {
        return a.equals(p) || b.equals(p) || c.equals(p);
    }

    /**
     * Tells whether this triangle shares at least one vertex with a
     * given super triangle, identified by its three corner points.
     * Used to remove the artificial super triangle at the end of the
     * algorithm.
     *
     * @param p1 first corner of the super triangle
     * @param p2 second corner of the super triangle
     * @param p3 third corner of the super triangle
     * @return true when this triangle touches the super triangle
     */
    public boolean sharesVertexWith(Point p1, Point p2, Point p3) {
        return hasVertex(p1) || hasVertex(p2) || hasVertex(p3);
    }

    /**
     * Returns a readable representation for the command line interface.
     *
     * @return a string describing the three vertices
     */
    @Override
    public String toString() {
        return "Triangle[" + a + ", " + b + ", " + c + "]";
    }
}

