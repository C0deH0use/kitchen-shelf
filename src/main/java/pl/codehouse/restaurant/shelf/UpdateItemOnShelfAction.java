package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an action to update an existing item on the shelf.
 * This record implements the ShelfAction interface and encapsulates the necessary information
 * to modify the quantity of a menu item on the shelf.
 */
public record UpdateItemOnShelfAction(
        @JsonProperty("menuItemId") int menuItemId,
        @JsonProperty("updateType") UpdateType updateType,
        @JsonProperty("quantity") int quantity) implements ShelfAction {

}
