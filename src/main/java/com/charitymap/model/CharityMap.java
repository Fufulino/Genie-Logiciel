package com.charitymap.model;

import com.charitymap.geometry.DelaunayTriangulator;
import com.charitymap.geometry.Triangle;
import com.charitymap.geometry.VoronoiBuilder;
import com.charitymap.geometry.VoronoiCell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The central object of the application: it owns every entity
 * (associations, centers, beneficiaries) and exposes the business
 * operations the user interface calls.
 * <p>
 * Because each center provides a single aid type, one Voronoi diagram
 * is built per aid type. A beneficiary is linked to the closest center
 * that provides the kind of help that beneficiary needs.
 *
 * @author CharityMap team
 */
public class CharityMap implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** Every association on the map. */
    private final List<Association> associations;

    /** Every distribution center on the map. */
    private final List<DistributionCenter> centers;

    /** Every beneficiary on the map. */
    private final List<Beneficiary> beneficiaries;

    /** The current Delaunay triangulation per aid type. */
    private transient Map<AidType, List<Triangle>> trianglesByType;

    /** The current Voronoi coverage zones per aid type. */
    private transient Map<AidType, List<VoronoiCell>> cellsByType;

    /**
     * Builds an empty map.
     */
    public CharityMap() {
        this.associations = new ArrayList<>();
        this.centers = new ArrayList<>();
        this.beneficiaries = new ArrayList<>();

        this.trianglesByType = new EnumMap<>(AidType.class);
        this.cellsByType = new EnumMap<>(AidType.class);
    }

    /**
     * Registers a new association.
     *
     * @param association the association to add
     */
    public void addAssociation(Association association) {
        if (association != null && !associations.contains(association)) {
            associations.add(association);
        }
    }

    /**
     * Adds a distribution center and recomputes the map so the
     * coverage zones stay consistent.
     *
     * @param center the center to add
     */
    public void addCenter(DistributionCenter center) {
        if (center != null && !centers.contains(center)) {
            centers.add(center);
            if (!associations.contains(center.getAssociation())) {
                associations.add(center.getAssociation());
            }
            recompute();
        }
    }

    /**
     * Removes a distribution center and recomputes the map.
     *
     * @param center the center to remove
     */
    public void removeCenter(DistributionCenter center) {
        if (centers.remove(center)) {
            center.getAssociation().removeCenter(center);
            recompute();
        }
    }

    /**
     * Adds a beneficiary and links it to the closest suitable center.
     *
     * @param beneficiary the beneficiary to add
     */
    public void addBeneficiary(Beneficiary beneficiary) {
        if (beneficiary != null && !beneficiaries.contains(beneficiary)) {
            beneficiaries.add(beneficiary);
            assignBeneficiaries();
        }
    }

    /**
     * Removes a beneficiary and refreshes the assignments.
     *
     * @param beneficiary the beneficiary to remove
     */
    public void removeBeneficiary(Beneficiary beneficiary) {
        if (beneficiaries.remove(beneficiary)) {
            assignBeneficiaries();
        }
    }



    /**
     * Clears all centers, beneficiaries, associations, and distributors.
     */
    public void clear() {
        this.centers.clear();
        this.beneficiaries.clear();
        this.associations.clear();

        recompute();
    }

    /**
     * Recomputes the whole map: one Delaunay triangulation and one
     * Voronoi diagram per aid type, then the beneficiary assignments.
     */
    public void recompute() {
        if (trianglesByType == null) {
            trianglesByType = new EnumMap<>(AidType.class);
        }
        if (cellsByType == null) {
            cellsByType = new EnumMap<>(AidType.class);
        }
        trianglesByType.clear();
        cellsByType.clear();

        // Clamp coordinates of all centers and beneficiaries to ensure they stay within [0, 1000] boundaries
        for (DistributionCenter c : centers) {
            double cx = Math.max(0.0, Math.min(1000.0, c.getPosition().getX()));
            double cy = Math.max(0.0, Math.min(1000.0, c.getPosition().getY()));
            c.setPosition(new Point(cx, cy));
        }
        for (Beneficiary b : beneficiaries) {
            double bx = Math.max(0.0, Math.min(1000.0, b.getPosition().getX()));
            double by = Math.max(0.0, Math.min(1000.0, b.getPosition().getY()));
            b.setPosition(new Point(bx, by));
        }

        DelaunayTriangulator triangulator = new DelaunayTriangulator();
        VoronoiBuilder voronoiBuilder = new VoronoiBuilder();

        for (AidType type : AidType.values()) {
            List<DistributionCenter> typeCenters = centersOfType(type);
            List<Point> positions = new ArrayList<>();
            for (DistributionCenter c : typeCenters) {
                positions.add(c.getPosition());
            }
            List<Triangle> allTriangles = triangulator.triangulate(positions, true);
            List<Triangle> filteredTriangles = triangulator.triangulate(positions, false);
            List<VoronoiCell> cells =
                    voronoiBuilder.build(typeCenters, allTriangles);
            trianglesByType.put(type, filteredTriangles);
            cellsByType.put(type, cells);
        }
        assignBeneficiaries();
    }

    /**
     * Moves a center to a new position and recomputes the map.
     *
     * @param center      the center to move
     * @param newPosition its new position
     */
    public void moveCenter(DistributionCenter center, Point newPosition) {
        center.setPosition(newPosition);
        recompute();
    }

    /**
     * Moves a beneficiary to a new position and recomputes assignments.
     *
     * @param beneficiary the beneficiary to move
     * @param newPosition its new position
     */
    public void moveBeneficiary(Beneficiary beneficiary, Point newPosition) {
        beneficiary.setPosition(newPosition);
        assignBeneficiaries();
    }

    /**
     * Links every beneficiary to the closest center that provides the
     * kind of help that beneficiary needs.
     */
    public void assignBeneficiaries() {
        for (DistributionCenter center : centers) {
            center.clearBeneficiaries();
        }
        for (Beneficiary beneficiary : beneficiaries) {
            DistributionCenter closest = findClosestCenter(
                    beneficiary.getPosition(), beneficiary.getNeed());
            beneficiary.setAssignedCenter(closest);
            if (closest != null) {
                closest.linkBeneficiary(beneficiary);
            }
        }
    }

    /**
     * Finds the closest center of a given aid type to a position.
     *
     * @param from the position to start from
     * @param type the required aid type
     * @return the closest matching center, or null when none exists
     */
    public DistributionCenter findClosestCenter(Point from, AidType type) {
        DistributionCenter best = null;
        double bestDistance = Double.MAX_VALUE;
        for (DistributionCenter center : centers) {
            if (center.getAidType() != type) {
                continue;
            }
            double d = from.squaredDistanceTo(center.getPosition());
            if (d < bestDistance) {
                bestDistance = d;
                best = center;
            }
        }
        return best;
    }

    /**
     * Returns every center providing a given aid type.
     *
     * @param type the aid type
     * @return the matching centers
     */
    public List<DistributionCenter> centersOfType(AidType type) {
        List<DistributionCenter> result = new ArrayList<>();
        for (DistributionCenter center : centers) {
            if (center.getAidType() == type) {
                result.add(center);
            }
        }
        return result;
    }

    /**
     * Returns the coverage zones of a given aid type.
     *
     * @param type the aid type
     * @return the coverage zones, never null
     */
    public List<VoronoiCell> getCells(AidType type) {
        if (cellsByType == null) {
            return new ArrayList<>();
        }
        List<VoronoiCell> cells = cellsByType.get(type);
        return (cells == null) ? new ArrayList<>() : cells;
    }

    /**
     * Returns the Delaunay triangulation of a given aid type.
     *
     * @param type the aid type
     * @return the triangulation, never null
     */
    public List<Triangle> getTriangles(AidType type) {
        if (trianglesByType == null) {
            return new ArrayList<>();
        }
        List<Triangle> triangles = trianglesByType.get(type);
        return (triangles == null) ? new ArrayList<>() : triangles;
    }

    /**
     * Returns every triangle across all aid types.
     *
     * @return the complete list of triangles
     */
    public List<Triangle> getAllTriangles() {
        List<Triangle> all = new ArrayList<>();
        if (trianglesByType == null) {
            return all;
        }
        for (List<Triangle> list : trianglesByType.values()) {
            all.addAll(list);
        }
        return all;
    }

    /**
     * Returns every Voronoi cell across all aid types.
     *
     * @return the complete list of cells
     */
    public List<VoronoiCell> getAllCells() {
        List<VoronoiCell> all = new ArrayList<>();
        if (cellsByType == null) {
            return all;
        }
        for (List<VoronoiCell> list : cellsByType.values()) {
            all.addAll(list);
        }
        return all;
    }

    /**
     * Returns every association.
     *
     * @return a copy of the associations list
     */
    public List<Association> getAssociations() {
        return new ArrayList<>(associations);
    }

    /**
     * Returns every center.
     *
     * @return a copy of the centers list
     */
    public List<DistributionCenter> getCenters() {
        return new ArrayList<>(centers);
    }

    /**
     * Returns every beneficiary.
     *
     * @return a copy of the beneficiaries list
     */
    public List<Beneficiary> getBeneficiaries() {
        return new ArrayList<>(beneficiaries);
    }
}