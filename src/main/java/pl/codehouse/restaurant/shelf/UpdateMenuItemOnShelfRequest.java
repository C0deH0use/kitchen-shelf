package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a request to update a menu item on the shelf.
 * This record encapsulates the update type and quantity for the operation.
 */
public record UpdateMenuItemOnShelfRequest(
        @JsonProperty("updateType") @NotNull UpdateType updateType,
        @JsonProperty("quantity") @Min(1) int quantity
) {
}
