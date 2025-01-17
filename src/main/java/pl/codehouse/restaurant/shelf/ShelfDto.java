package pl.codehouse.restaurant.shelf;

public record ShelfDto(
        String menuItemName,
        int menuItemId,
        int quantity,
        long version) {

    static ShelfDto from(ShelfEntity entity) {
        return new ShelfDto(entity.menuItemName(), entity.menuItemId(), entity.quantity(), entity.version());
    }
}
