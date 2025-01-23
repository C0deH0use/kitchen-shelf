package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a request to update a menu item on the shelf.
 * This record encapsulates the update type and quantity for the operation.
 */
public record UpdateMenuItemOnShelfRequest(
        /**
         * The type of update operation to perform.
         * Must not be null.
         */
        @JsonProperty("updateType") @NotNull UpdateType updateType,

        /**
         * The quantity to update.
         * Must be a positive integer (minimum value of 1).
         */
        @JsonProperty("quantity") @Min(1) int quantity
) {
}
