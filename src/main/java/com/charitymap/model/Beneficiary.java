package com.charitymap.model;

import java.io.Serializable;

/**
 * A beneficiary: a person or household in need of help.
 * <p>
 * A beneficiary is a mobile point on the map. It has a single aid need
 * and is automatically linked to the closest distribution center that
 * provides that exact kind of help. This link is the social heart of
 * the project: the distance of this link measures how far someone in
 * need must travel to be helped.
 *
 * @author CharityMap team
 */
public class Beneficiary implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** A counter used to give every beneficiary a unique identifier. */
    private static int nextId = 1;

    /** The unique identifier of this beneficiary. */
    private final int id;

    /** The current position of this beneficiary on the map. */
    private Point position;

    /** The kind of help this beneficiary needs. */
    private final AidType need;

    /** The center this beneficiary is currently linked to (may be null). */
    private DistributionCenter assignedCenter;

    /**
     * Builds a new beneficiary.
     *
     * @param position the position on the map (must not be null)
     * @param need     the kind of help needed (must not be null)
     */
    public Beneficiary(Point position, AidType need) {
        if (position == null || need == null) {
            throw new IllegalArgumentException(
                    "A beneficiary needs a position and an aid need.");
        }
        this.id = nextId++;
        this.position = position;
        this.need = need;
        this.assignedCenter = null;
    }

    /**
     * Returns the unique identifier of this beneficiary.
     *
     * @return the identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the current position of this beneficiary.
     *
     * @return the position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Moves this beneficiary to a new position.
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
     * Returns the kind of help this beneficiary needs.
     *
     * @return the aid need
     */
    public AidType getNeed() {
        return need;
    }

    /**
     * Returns the center this beneficiary is linked to.
     *
     * @return the assigned center, or null when none was assigned yet
     */
    public DistributionCenter getAssignedCenter() {
        return assignedCenter;
    }

    /**
     * Sets the center this beneficiary is linked to. Called by the map
     * during the assignment step.
     *
     * @param center the center to link to (may be null)
     */
    public void setAssignedCenter(DistributionCenter center) {
        this.assignedCenter = center;
    }

    /**
     * Returns the distance to the assigned center, or a negative value
     * when this beneficiary has no assigned center.
     *
     * @return the travel distance, or -1 when not assigned
     */
    public double distanceToAssignedCenter() {
        if (assignedCenter == null) {
            return -1.0;
        }
        return position.distanceTo(assignedCenter.getPosition());
    }

    /**
     * Returns a readable representation for the command line interface.
     *
     * @return a string describing this beneficiary
     */
    @Override
    public String toString() {
        String target = (assignedCenter == null)
                ? "no center"
                : ("center #" + assignedCenter.getId());
        return "Beneficiary #" + id + " " + position
                + " needs " + need.getLabel() + ", assigned to " + target;
    }
}

