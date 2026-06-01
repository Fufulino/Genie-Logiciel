package com.cergy.charitymap.geometry;

import com.cergy.charitymap.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a Delaunay triangulation of a set of points using the
 * Bowyer-Watson incremental algorithm.
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

        // TODO: Implement super-triangle and Bowyer-Watson incremental loop
        
        return triangulation;
    }
}