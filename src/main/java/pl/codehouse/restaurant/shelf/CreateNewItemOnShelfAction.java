package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents an action to create a new item on the shelf.
 * This record implements the ShelfAction interface and encapsulates the necessary information
 * to add a new menu item to the shelf.
 */
public record CreateNewItemOnShelfAction(
        @JsonProperty("menuItemId") @Min(1) int menuItemId,
        @JsonProperty("menuItemName") @NotBlank String menuItemName,
        @JsonProperty("quantity") @Min(1) int quantity) implements ShelfAction {
}
