package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateNewItemOnShelfAction(
        @JsonProperty("menuItemId") int menuItemId,
        @JsonProperty("menuItemName") String menuItemName,
        @JsonProperty("quantity") int quantity) implements ShelfAction {
}
