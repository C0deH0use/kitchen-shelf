package pl.codehouse.restaurant.shelf;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing shelf operations.
 * This class handles HTTP requests related to shelf items, including fetching, updating, and adding items.
 */
@Validated
@RestController
@RequestMapping(value = "/shelf",
        consumes = {MediaType.APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
class ShelfResource {
    private final ShelfQueryService queryService;
    private final ShelfService shelfService;

    /**
     * Constructs a new ShelfResource with the given services.
     *
     * @param queryService The service for querying shelf items.
     * @param shelfService The service for performing actions on shelf items.
     */
    ShelfResource(ShelfQueryService queryService, ShelfService shelfService) {
        this.queryService = queryService;
        this.shelfService = shelfService;
    }

    /**
     * Fetches all available items on the shelf.
     *
     * @return A Mono emitting a List of ShelfDto objects representing available items.
     */
    @GetMapping
    Mono<List<ShelfDto>> fetchAvailableItemsOnShelf() {
        return queryService.findAllAvailableItems();
    }

    /**
     * Fetches a specific item from the shelf by its menu item ID.
     *
     * @param menuItemId The ID of the menu item to fetch.
     * @return A Mono emitting the ShelfDto for the specified menu item.
     */
    @GetMapping("/{menuItemId}")
    Mono<ShelfDto> fetchByMenuItem(@PathVariable
                                   @Positive Integer menuItemId) {
        return queryService.findByMenuItemId(menuItemId);
    }

    /**
     * Updates an existing item on the shelf.
     *
     * @param menuItemId The ID of the menu item to update.
     * @param request The update request containing the update type and quantity.
     * @return A Mono emitting the updated ShelfDto.
     */
    @PutMapping("/{menuItemId}")
    Mono<ShelfDto> updateByMenuItem(@PathVariable
                                    @Positive Integer menuItemId,
                                    @RequestBody
                                    @NotNull
                                    @Valid UpdateMenuItemOnShelfRequest request) {
        UpdateItemOnShelfAction action = new UpdateItemOnShelfAction(menuItemId, request.updateType(), request.quantity());
        return shelfService.action(action);
    }

    /**
     * Adds a new item to the shelf.
     *
     * @param action The action containing details of the new item to add.
     * @return A Mono emitting the newly created ShelfDto.
     */
    @PostMapping
    @ResponseStatus(CREATED)
    Mono<ShelfDto> addByMenuItem(@RequestBody @NotNull @Valid CreateNewItemOnShelfAction action) {
        return shelfService.action(action);
    }
}
