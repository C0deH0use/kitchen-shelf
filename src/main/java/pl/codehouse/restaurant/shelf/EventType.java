package pl.codehouse.restaurant.shelf;

/**
 * Enum representing the types of events that can occur on a shelf item.
 * This enum is used to categorize different actions performed on shelf items.
 */
public enum EventType {
    /**
     * Indicates that a new item has been added to the shelf.
     */
    NEW,

    /**
     * Indicates that the quantity of an existing item on the shelf has been increased.
     */
    ADD,

    /**
     * Indicates that some quantity of an item has been taken from the shelf.
     */
    TAKE
}
