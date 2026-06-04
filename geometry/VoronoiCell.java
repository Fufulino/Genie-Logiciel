package com.cergy.charitymap.geometry;

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
}
