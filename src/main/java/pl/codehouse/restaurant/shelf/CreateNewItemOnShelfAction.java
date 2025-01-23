package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents an action to create a new item on the shelf.
 * This record implements the ShelfAction interface and encapsulates the necessary information
 * to add a new menu item to the shelf.
 */
/**
 * Represents an action to create a new item on the shelf.
 * This record implements the ShelfAction interface and encapsulates the necessary information
 * to add a new menu item to the shelf.
 */
public record CreateNewItemOnShelfAction(
        /**
         * The unique identifier of the menu item.
         * Must be a positive integer.
         */
        @JsonProperty("menuItemId") @Min(1) int menuItemId,

        /**
         * The name of the menu item.
         * Must not be blank.
         */
        @JsonProperty("menuItemName") @NotBlank String menuItemName,

        /**
         * The initial quantity of the menu item to be added to the shelf.
         * Must be a positive integer.
         */
        @JsonProperty("quantity") @Min(1) int quantity) implements ShelfAction {
}
