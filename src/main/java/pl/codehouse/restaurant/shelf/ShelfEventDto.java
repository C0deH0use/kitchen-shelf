package pl.codehouse.restaurant.shelf;

public record ShelfEventDto(
        EventType eventType,
        int menuItemId,
        int quantity) {
}
