package pl.codehouse.restaurant.shelf;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Shelf Service for Query related actions.
 */
public interface ShelfQueryService {

    /**
     * Fetch Shelf object by menuItemId.
     *
     * @param menuItemId by which we do the query in db.
     * @return ShelfDto.
     */
    @Transactional(readOnly = true)
    Mono<ShelfDto> findByMenuItemId(int menuItemId);

    @Transactional(readOnly = true)
    Mono<List<ShelfDto>> findAllAvailableItems();
}
