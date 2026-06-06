package com.cergy.charitymap.model;

import java.io.Serializable;

/**
 * A distributor: a mobile member of an association who carries out
 * the actual distribution on the ground.
 * <p>
 * A distributor is attached to a distribution center and can move on
 * the map. The ratio between the number of beneficiaries in a coverage
 * zone and the number of distributors working there is a strong social
 * indicator: a zone with many people in need and few distributors is a
 * point of tension.
 *
 * @author CharityMap team
 */
public class Distributor implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** A counter used to give every distributor a unique identifier. */
    private static int nextId = 1;

    /** The unique identifier of this distributor. */
    private final int id;

    /** The current position of this distributor on the map. */
    private Point position;

    /** The center this distributor works from. */
    private DistributionCenter homeCenter;

    /**
     * Builds a new distributor and registers it on its home center.
     *
     * @param position   the position on the map (must not be null)
     * @param homeCenter the center this distributor works from
     *                   (must not be null)
     */
    public Distributor(Point position, DistributionCenter homeCenter) {
        if (position == null || homeCenter == null) {
            throw new IllegalArgumentException(
                    "A distributor needs a position and a home center.");
        }
        this.id = nextId++;
        this.position = position;
        this.homeCenter = homeCenter;
        homeCenter.addDistributor(this);
    }

    /**
     * Returns the unique identifier of this distributor.
     *
     * @return the identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the current position of this distributor.
     *
     * @return the position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Moves this distributor to a new position.
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
     * Returns the center this distributor works from.
     *
     * @return the home center
     */
    public DistributionCenter getHomeCenter() {
        return homeCenter;
    }

    /**
     * Reassigns this distributor to another home center.
     *
     * @param newHomeCenter the new home center (must not be null)
     */
    public void setHomeCenter(DistributionCenter newHomeCenter) {
        if (newHomeCenter == null) {
            throw new IllegalArgumentException("Home center must not be null.");
        }
        this.homeCenter.removeDistributor(this);
        this.homeCenter = newHomeCenter;
        newHomeCenter.addDistributor(this);
    }

    /**
     * Returns a readable representation for the command line interface.
     *
     * @return a string describing this distributor
     */
    @Override
    public String toString() {
        return "Distributor #" + id + " " + position
                + " from center #" + homeCenter.getId();
    }
}
