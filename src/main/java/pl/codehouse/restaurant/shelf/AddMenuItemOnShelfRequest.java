package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddMenuItemOnShelfRequest(
        @JsonProperty("menuItemId") @Min(1000) int menuItemId,
        @JsonProperty("menuItemName") @NotBlank String menuItemName,
        @JsonProperty("quantity") @Min(1) int quantity
) {
}
