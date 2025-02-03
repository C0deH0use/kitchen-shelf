package pl.codehouse.restaurant.shelf;

import pl.codehouse.commons.ActionEvent;
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
    Mono<ShelfDto> action(ActionEvent action);
}
