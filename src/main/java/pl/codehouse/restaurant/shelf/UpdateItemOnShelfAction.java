package pl.codehouse.restaurant.shelf;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateItemOnShelfAction(
        @JsonProperty("menuItemId") int menuItemId,
        @JsonProperty("updateType") UpdateType updateType,
        @JsonProperty("quantity") int quantity) implements ShelfAction {
}
