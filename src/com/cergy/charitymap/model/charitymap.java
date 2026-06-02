package com.cergy.charitymap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The central object of the application: it owns every entity
 * (associations, centers, beneficiaries, distributors) and exposes the
 * business operations the user interface calls.
 * <p>
 * This first version only handles storage and listing. The geometric
 * computations (Delaunay triangulation, Voronoi cells and beneficiary
 * assignments) will be plugged in later.
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

    /** Every distributor on the map. */
    private final List<Distributor> distributors;

    /**
     * Builds an empty map.
     */
    public CharityMap() {
        this.associations = new ArrayList<>();
        this.centers = new ArrayList<>();
        this.beneficiaries = new ArrayList<>();
        this.distributors = new ArrayList<>();
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
     * Adds a distribution center to the map.
     *
     * @param center the center to add
     */
    public void addCenter(DistributionCenter center) {
        if (center != null && !centers.contains(center)) {
            centers.add(center);
            if (!associations.contains(center.getAssociation())) {
                associations.add(center.getAssociation());
            }
        }
    }

    /**
     * Removes a distribution center from the map.
     *
     * @param center the center to remove
     */
    public void removeCenter(DistributionCenter center) {
        if (centers.remove(center)) {
            center.getAssociation().removeCenter(center);
        }
    }

    /**
     * Adds a beneficiary to the map.
     *
     * @param beneficiary the beneficiary to add
     */
    public void addBeneficiary(Beneficiary beneficiary) {
        if (beneficiary != null && !beneficiaries.contains(beneficiary)) {
            beneficiaries.add(beneficiary);
        }
    }

    /**
     * Removes a beneficiary from the map.
     *
     * @param beneficiary the beneficiary to remove
     */
    public void removeBeneficiary(Beneficiary beneficiary) {
        beneficiaries.remove(beneficiary);
    }

    /**
     * Registers a distributor on the map.
     *
     * @param distributor the distributor to add
     */
    public void addDistributor(Distributor distributor) {
        if (distributor != null && !distributors.contains(distributor)) {
            distributors.add(distributor);
        }
    }

    /**
     * Removes a distributor from the map.
     *
     * @param distributor the distributor to remove
     */
    public void removeDistributor(Distributor distributor) {
        if (distributors.remove(distributor)) {
            distributor.getHomeCenter().removeDistributor(distributor);
        }
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

    /**
     * Returns every distributor.
     *
     * @return a copy of the distributors list
     */
    public List<Distributor> getDistributors() {
        return new ArrayList<>(distributors);
    }
}