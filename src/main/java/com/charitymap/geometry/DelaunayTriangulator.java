package com.charitymap.geometry;

import com.charitymap.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a Delaunay triangulation of a set of points using the
 * Bowyer-Watson incremental algorithm.
 * <p>
 * The algorithm is intentionally written in a step by step, academic
 * style so that every member of the team can explain it during the
 * oral examination. The idea is the following:
 * <ol>
 *   <li>Start with one huge "super triangle" that contains every
 *       input point.</li>
 *   <li>Insert the points one by one. For each new point, find all the
 *       triangles whose circumscribed circle contains it. These
 *       triangles are "bad" and form a polygonal hole.</li>
 *   <li>Remove the bad triangles, then fill the hole by joining the
 *       new point to every edge of the boundary of that hole.</li>
 *   <li>At the end, drop every triangle still connected to the super
 *       triangle.</li>
 * </ol>
 *
 * @author CharityMap team
 */
public class DelaunayTriangulator {

    /**
     * A simple pair of points representing one edge of a triangle.
     * Used while rebuilding the boundary of the polygonal hole.
     */
    private static final class Edge {

        /** First endpoint of the edge. */
        private final Point p1;

        /** Second endpoint of the edge. */
        private final Point p2;

        /**
         * Builds an edge from two points.
         *
         * @param p1 the first endpoint
         * @param p2 the second endpoint
         */
        private Edge(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        /**
         * Two edges are the same even when their endpoints are given
         * in the opposite order.
         *
         * @param other the edge to compare with
         * @return true when both edges join the same two points
         */
        private boolean sameAs(Edge other) {
            return (this.p1.equals(other.p1) && this.p2.equals(other.p2))
                    || (this.p1.equals(other.p2) && this.p2.equals(other.p1));
        }
    }

    /**
     * Computes the Delaunay triangulation of the given points.
     *
     * @param points the input points (sites). May be empty.
     * @return the list of triangles of the triangulation
     */
    public List<Triangle> triangulate(List<Point> points) {
        List<Triangle> triangulation = new ArrayList<>();

        // Fewer than three points cannot form any triangle.
        if (points == null || points.size() < 3) {
            return triangulation;
        }

        // Step 1: build a super triangle large enough to wrap every
        // point. We look at the bounding box of the points then push
        // its corners far away.
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }
        double deltaX = maxX - minX;
        double deltaY = maxY - minY;
        double deltaMax = Math.max(deltaX, deltaY);
        // Guard against the case where every point is identical.
        if (deltaMax == 0.0) {
            deltaMax = 1.0;
        }
        double midX = (minX + maxX) / 2.0;
        double midY = (minY + maxY) / 2.0;

        Point superA = new Point(midX - 20 * deltaMax, midY - deltaMax);
        Point superB = new Point(midX, midY + 20 * deltaMax);
        Point superC = new Point(midX + 20 * deltaMax, midY - deltaMax);

        triangulation.add(new Triangle(superA, superB, superC));

        // Step 2: insert each point one by one.
        for (Point point : points) {
            List<Triangle> badTriangles = new ArrayList<>();

            // Find every triangle whose circumscribed circle contains
            // the new point.
            for (Triangle triangle : triangulation) {
                if (triangle.circumcircleContains(point)) {
                    badTriangles.add(triangle);
                }
            }

            // Build the boundary of the polygonal hole. An edge belongs
            // to the boundary when it is shared by exactly one bad
            // triangle.
            List<Edge> boundary = new ArrayList<>();
            for (Triangle triangle : badTriangles) {
                Edge[] edges = {
                        new Edge(triangle.getA(), triangle.getB()),
                        new Edge(triangle.getB(), triangle.getC()),
                        new Edge(triangle.getC(), triangle.getA())
                };
                for (Edge edge : edges) {
                    boolean shared = false;
                    for (Triangle other : badTriangles) {
                        if (other == triangle) {
                            continue;
                        }
                        Edge[] otherEdges = {
                                new Edge(other.getA(), other.getB()),
                                new Edge(other.getB(), other.getC()),
                                new Edge(other.getC(), other.getA())
                        };
                        for (Edge otherEdge : otherEdges) {
                            if (edge.sameAs(otherEdge)) {
                                shared = true;
                                break;
                            }
                        }
                        if (shared) {
                            break;
                        }
                    }
                    if (!shared) {
                        boundary.add(edge);
                    }
                }
            }

            // Remove the bad triangles from the triangulation.
            triangulation.removeAll(badTriangles);

            // Fill the hole: connect the new point to every boundary
            // edge.
            for (Edge edge : boundary) {
                triangulation.add(new Triangle(edge.p1, edge.p2, point));
            }
        }

        // Step 3: drop every triangle that still touches the super
        // triangle, since those vertices are artificial.
        List<Triangle> result = new ArrayList<>();
        for (Triangle triangle : triangulation) {
            if (!triangle.sharesVertexWith(superA, superB, superC)) {
                result.add(triangle);
            }
        }
        return result;
    }
}
