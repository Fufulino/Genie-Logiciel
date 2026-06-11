package com.charitymap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A physical distribution center: the main premises of an association.
 * <p>
 * On the map this is a fixed reference point, that is to say a Voronoi
 * site. The Voronoi cell built around it represents its coverage zone:
 * every location inside that zone is closer to this center than to any
 * other center of the same aid type.
 *
 * @author CharityMap team
 */
public class DistributionCenter implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** A counter used to give every center a unique identifier. */
    private static int nextId = 1;

    /** The unique identifier of this center. */
    private final int id;

    /** The fixed position of this center on the map. */
    private Point position;

    /** The association this center belongs to. */
    private final Association association;

    /** The beneficiaries currently linked to this center. */
    private final List<Beneficiary> linkedBeneficiaries;

    /**
     * Builds a new distribution center and registers it on its
     * association automatically.
     *
     * @param position    the position on the map (must not be null)
     * @param association the owning association (must not be null)
     */
    public DistributionCenter(Point position, Association association) {
        if (position == null || association == null) {
            throw new IllegalArgumentException(
                    "A center needs a position and an association.");
        }
        this.id = nextId++;
        this.position = position;
        this.association = association;
        this.linkedBeneficiaries = new ArrayList<>();
        association.addCenter(this);
    }

    /**
     * Returns the unique identifier of this center.
     *
     * @return the identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the current position of this center.
     *
     * @return the position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Moves this center to a new position. The Voronoi diagram must be
     * recomputed afterwards so that the map stays consistent.
     *
     * @param newPosition the new position (must not be null)
     */
    public void setPosition(Point newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("Position must not be null.");
        }
        this.position = newPosition;
    }

    /**
     * Returns the association this center belongs to.
     *
     * @return the association
     */
    public Association getAssociation() {
        return association;
    }

    /**
     * Convenience accessor for the aid type of this center.
     *
     * @return the aid type provided here
     */
    public AidType getAidType() {
        return association.getAidType();
    }

    /**
     * Returns a copy of the beneficiaries linked to this center.
     *
     * @return the linked beneficiaries
     */
    public List<Beneficiary> getLinkedBeneficiaries() {
        return new ArrayList<>(linkedBeneficiaries);
    }

    /**
     * Links a beneficiary to this center. Called by the map when a
     * beneficiary is assigned to its closest center.
     *
     * @param beneficiary the beneficiary to link
     */
    public void linkBeneficiary(Beneficiary beneficiary) {
        if (beneficiary != null && !linkedBeneficiaries.contains(beneficiary)) {
            linkedBeneficiaries.add(beneficiary);
        }
    }

    /**
     * Removes every beneficiary link. Called before recomputing all
     * the assignments from scratch.
     */
    public void clearBeneficiaries() {
        linkedBeneficiaries.clear();
    }

    /**
     * Returns a readable representation for the command line interface.
     *
     * @return a string describing this center
     */
    @Override
    public String toString() {
        return "Center #" + id + " " + position
                + " of " + association;
    }
}

