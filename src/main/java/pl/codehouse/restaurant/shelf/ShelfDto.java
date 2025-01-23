package pl.codehouse.restaurant.shelf;

/**
 * Data Transfer Object (DTO) representing a shelf item.
 * This record encapsulates the essential information about an item on the shelf,
 * including its name, ID, quantity, and version.
 */
public record ShelfDto(
        String menuItemName,
        int menuItemId,
        int quantity,
        long version) {

    /**
     * Creates a ShelfDto from a ShelfEntity.
     *
     * @param entity The ShelfEntity to convert.
     * @return A new ShelfDto instance with data from the given entity.
     */
    static ShelfDto from(ShelfEntity entity) {
        return new ShelfDto(entity.menuItemName(), entity.menuItemId(), entity.quantity(), entity.version());
    }
}
