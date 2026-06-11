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

        DelaunayTriangulator triangulator = new DelaunayTriangulator();
        VoronoiBuilder voronoiBuilder = new VoronoiBuilder();

        for (AidType type : AidType.values()) {
            List<DistributionCenter> typeCenters = centersOfType(type);
            List<Point> positions = new ArrayList<>();
            for (DistributionCenter c : typeCenters) {
                positions.add(c.getPosition());
            }
            List<Triangle> triangles = triangulator.triangulate(positions);
            List<VoronoiCell> cells =
                    voronoiBuilder.build(typeCenters, triangles);
            trianglesByType.put(type, triangles);
            cellsByType.put(type, cells);
        }
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
     * Returns all currently computed Voronoi cells for every aid type.
     *
     * @return all cells, never null
     */
    public List<VoronoiCell> getAllCells() {
        List<VoronoiCell> allCells = new ArrayList<>();
        for (AidType type : AidType.values()) {
            allCells.addAll(getCells(type));
        }
        return allCells;
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

