package pl.codehouse.restaurant.shelf;

import reactor.core.publisher.Mono;

/**
 * Shelf Service for state change related actions.
 */
public interface ShelfService {

    /**
     * Update given Shelf object.
     *
     * @param action by which we do the update.
     * @return ShelfDto after the action is performed.
     */
    Mono<ShelfDto> action(ShelfAction action);
}
