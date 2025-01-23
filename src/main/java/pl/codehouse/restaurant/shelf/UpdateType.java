package pl.codehouse.restaurant.shelf;

/**
 * Enum representing the types of updates that can be performed on a shelf item.
 */
public enum UpdateType {
    /**
     * Indicates that the quantity should be added to the current amount.
     */
    ADD,

    /**
     * Indicates that the quantity should be subtracted from the current amount.
     */
    TAKE
}
