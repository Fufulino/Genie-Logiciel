package com.charitymap.geometry;

import com.charitymap.model.Point;

import java.util.List;

/**
 * A small toolbox of pure geometric computations.
 * <p>
 * Every method here is static and has no side effect: it takes points
 * as input and returns a result. This makes the class easy to test on
 * the command line and easy to defend during the oral examination.
 *
 * @author CharityMap team
 */
public final class GeometryUtils {

    /** This class only holds static helpers, so it must not be built. */
    private GeometryUtils() {
    }

    /**
     * Computes the circumcenter of the triangle defined by three points,
     * that is to say the center of the unique circle passing through the
     * three of them. In a Delaunay / Voronoi setting this circumcenter
     * is exactly a Voronoi vertex.
     *
     * @param a the first vertex
     * @param b the second vertex
     * @param c the third vertex
     * @return the circumcenter, or null when the three points are aligned
     */
    public static Point circumcenter(Point a, Point b, Point c) {
        double ax = a.getX();
        double ay = a.getY();
        double bx = b.getX();
        double by = b.getY();
        double cx = c.getX();
        double cy = c.getY();

        double d = 2.0 * (ax * (by - cy)
                + bx * (cy - ay)
                + cx * (ay - by));

        // When d is almost zero the three points are aligned and there
        // is no circumscribed circle.
        if (Math.abs(d) < 1e-12) {
            return null;
        }

        double ax2ay2 = ax * ax + ay * ay;
        double bx2by2 = bx * bx + by * by;
        double cx2cy2 = cx * cx + cy * cy;

        double ux = (ax2ay2 * (by - cy)
                + bx2by2 * (cy - ay)
                + cx2cy2 * (ay - by)) / d;

        double uy = (ax2ay2 * (cx - bx)
                + bx2by2 * (ax - cx)
                + cx2cy2 * (bx - ax)) / d;

        return new Point(ux, uy);
    }

    /**
     * Computes the area of a simple polygon using the shoelace formula.
     * The vertices must be ordered (clockwise or counter clockwise).
     *
     * @param vertices the ordered vertices of the polygon
     * @return the area, always returned as a positive value
     */
    public static double polygonArea(List<Point> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return 0.0;
        }
        double sum = 0.0;
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            Point current = vertices.get(i);
            Point next = vertices.get((i + 1) % n);
            sum += current.getX() * next.getY()
                    - next.getX() * current.getY();
        }
        return Math.abs(sum) / 2.0;
    }

    /**
     * Computes the centroid (average position) of a list of points.
     *
     * @param points the points (must not be null nor empty)
     * @return the centroid
     */
    public static Point centroid(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot compute the centroid of an empty list.");
        }
        double sumX = 0.0;
        double sumY = 0.0;
        for (Point p : points) {
            sumX += p.getX();
            sumY += p.getY();
        }
        return new Point(sumX / points.size(), sumY / points.size());
    }

    /**
     * Returns the polar angle of a point as seen from a reference
     * center. Used to sort the corners of a Voronoi cell so that the
     * polygon does not self intersect.
     *
     * @param center the reference center
     * @param p      the point whose angle is wanted
     * @return the angle in radians, between -PI and PI
     */
    public static double angleFromCenter(Point center, Point p) {
        return Math.atan2(p.getY() - center.getY(),
                p.getX() - center.getX());
    }
}

