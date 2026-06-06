package com.cergy.charitymap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A charity association operating in Cergy.
 * <p>
 * An association is deliberately kept separate from its physical
 * locations. The association carries the identity (a name and one aid
 * type); each {@link DistributionCenter} carries a position on the map.
 * One association may own several centers. This separation keeps the
 * door open to the "several locations per association" option without
 * any painful refactoring later.
 *
 * @author CharityMap team
 */
public class Association implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** The name of the association. */
    private final String name;

    /** The single kind of help this association provides. */
    private final AidType aidType;

    /** The physical centers owned by this association. */
    private final List<DistributionCenter> centers;

    /**
     * Builds a new association.
     *
     * @param name    the name of the association (must not be null)
     * @param aidType the kind of help it provides (must not be null)
     */
    public Association(String name, AidType aidType) {
        if (name == null || aidType == null) {
            throw new IllegalArgumentException(
                    "An association needs a name and an aid type.");
        }
        this.name = name;
        this.aidType = aidType;
        this.centers = new ArrayList<>();
    }

    /**
     * Returns the name of the association.
     *
     * @return the association name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the kind of help this association provides.
     *
     * @return the aid type
     */
    public AidType getAidType() {
        return aidType;
    }

    /**
     * Returns the list of centers owned by this association.
     * <p>
     * A copy is returned so that callers cannot modify the internal
     * list directly. Use {@link #addCenter(DistributionCenter)} for that.
     *
     * @return a copy of the list of centers
     */
    public List<DistributionCenter> getCenters() {
        return new ArrayList<>(centers);
    }

    /**
     * Adds a center to this association.
     *
     * @param center the center to add (must not be null)
     */
    public void addCenter(DistributionCenter center) {
        if (center == null) {
            throw new IllegalArgumentException("Center must not be null.");
        }
        if (!centers.contains(center)) {
            centers.add(center);
        }
    }

    /**
     * Removes a center from this association.
     *
     * @param center the center to remove
     */
    public void removeCenter(DistributionCenter center) {
        centers.remove(center);
    }

    /**
     * Returns a readable representation for the command line interface.
     *
     * @return a string such as "Restos du Coeur [Food]"
     */
    @Override
    public String toString() {
        return name + " [" + aidType.getLabel() + "]";
    }
}
