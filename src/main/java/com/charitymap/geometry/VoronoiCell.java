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

    
    /**
     * Returns the number of beneficiaries linked to this center.
     *
     * @return the number of covered beneficiaries
     */
    public int getBeneficiaryCount() {
        return center.getLinkedBeneficiaries().size();
    }

    /**
     * Returns the average distance a beneficiary of this zone has to
     * travel to reach the center. This is a strong territorial equity
     * indicator for the ethics scope.
     *
     * @return the average travel distance, or zero when no beneficiary
     */
    public double getAverageTravelDistance() {
        List<Beneficiary> people = center.getLinkedBeneficiaries();
        if (people.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Beneficiary b : people) {
            total += b.getPosition().distanceTo(center.getPosition());
        }
        return total / people.size();
    }

    /**
     * Returns the maximum distance any beneficiary of this zone has to
     * travel. A large value points at someone left behind.
     *
     * @return the maximum travel distance, or zero when no beneficiary
     */
    public double getMaxTravelDistance() {
        double max = 0.0;
        for (Beneficiary b : center.getLinkedBeneficiaries()) {
            double d = b.getPosition().distanceTo(center.getPosition());
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    /**
     * Builds a multi line report of the statistics of this zone, ready
     * to be printed by the command line interface.
     *
     * @return a human readable statistics report
     */
    public String statisticsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Coverage zone of ").append(center).append("\n");
        sb.append("  Covered beneficiaries: ")
                .append(getBeneficiaryCount()).append("\n");
        sb.append("  Average travel distance: ")
                .append(String.format("%.2f", getAverageTravelDistance()))
                .append("\n");
        sb.append("  Max travel distance    : ")
                .append(String.format("%.2f", getMaxTravelDistance()))
                .append("\n");
        sb.append("  Zone area              : ")
                .append(String.format("%.2f", getArea()));
        return sb.toString();
    }
}