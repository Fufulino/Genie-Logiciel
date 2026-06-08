package com.charitymap.geometry;

import com.charitymap.model.Beneficiary;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The coverage zone of one distribution center: its Voronoi cell.
 * <p>
 * Every location inside this cell is closer to this center than to any
 * other center of the same aid type. The cell also carries the social
 * statistics that make this project meaningful for the ethics and
 * design scope: how many beneficiaries are covered, how far they have
 * to travel, and how loaded the center is.
 *
 * @author CharityMap team
 */
public class VoronoiCell implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** The center this cell is built around. */
    private final DistributionCenter center;

    /** The ordered corners of the cell polygon (Voronoi vertices). */
    private final List<Point> corners;

    /**
     * Builds a coverage zone for a given center.
     *
     * @param center the center at the heart of this cell
     */
    public VoronoiCell(DistributionCenter center) {
        this.center = center;
        this.corners = new ArrayList<>();
    }

    /**
     * Returns the center this cell belongs to.
     *
     * @return the distribution center
     */
    public DistributionCenter getCenter() {
        return center;
    }

    /**
     * Returns the ordered corners of the cell polygon.
     *
     * @return the list of corners
     */
    public List<Point> getCorners() {
        return new ArrayList<>(corners);
    }

    /**
     * Adds a corner (a Voronoi vertex) to this cell. The corners are
     * sorted afterwards by {@link #sortCorners()}.
     *
     * @param corner the corner to add
     */
    public void addCorner(Point corner) {
        if (corner != null && !corners.contains(corner)) {
            corners.add(corner);
        }
    }

    /**
     * Sorts the corners by their angle around the center so that the
     * polygon is drawn without self intersection.
     */
    public void sortCorners() {
        Point reference = center.getPosition();
        corners.sort((p1, p2) -> {
            double a1 = GeometryUtils.angleFromCenter(reference, p1);
            double a2 = GeometryUtils.angleFromCenter(reference, p2);
            return Double.compare(a1, a2);
        });
    }

    /**
     * Returns the surface of this coverage zone. An unbounded or not
     * yet computed cell returns zero.
     *
     * @return the area of the cell
     */
    public double getArea() {
        return GeometryUtils.polygonArea(corners);
    }
}

