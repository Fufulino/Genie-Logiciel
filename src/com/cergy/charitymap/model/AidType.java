package com.cergy.charitymap.model;

/**
 * The kind of help a charity association provides in the city of Cergy.
 * <p>
 * This enum is the cornerstone of the whole domain model. A beneficiary
 * needing food must be linked to the closest food center, not simply to
 * the closest center of any kind. This is why one Voronoi diagram is
 * built per aid type.
 *
 * @author CharityMap team
 */
public enum AidType {

    /** Distribution of clothes. */
    CLOTHING("Clothing"),

    /** Distribution of food. */
    FOOD("Food"),

    /** Medical or social care. */
    CARE("Care");

    /** A human readable label, used in the command line interface. */
    private final String label;

    /**
     * Builds an aid type with a readable label.
     *
     * @param label the text shown to the user
     */
    AidType(String label) {
        this.label = label;
    }

    /**
     * Returns the readable label of this aid type.
     *
     * @return the label, for example "Food"
     */
    public String getLabel() {
        return label;
    }
}
