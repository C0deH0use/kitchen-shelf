package pl.codehouse.restaurant.shelf;

/**
 * Data Transfer Object (DTO) representing an event that occurred on a shelf item.
 * This record encapsulates the essential information about a shelf event,
 * including the type of event, the menu item ID, and the quantity involved.
 */
public record ShelfEventDto(
        EventType eventType,
        int menuItemId,
        int quantity) {
}
