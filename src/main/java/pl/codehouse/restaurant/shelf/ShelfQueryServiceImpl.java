package pl.codehouse.restaurant.shelf;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class ShelfQueryServiceImpl implements ShelfQueryService {
    private static final String ITEMS_BY_MENU_ITEM_ID = "itemsByMenuItemId";
    private static final String AVAILABLE_ITEMS = "availableItems";

    private final ShelfRepository shelfRepository;

    ShelfQueryServiceImpl(ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

    @Override
    @Cacheable(ITEMS_BY_MENU_ITEM_ID)
    public Mono<ShelfDto> findByMenuItemId(int menuItemId) {
        return shelfRepository.findByMenuItemId(menuItemId)
                .map(ShelfDto::from);
    }

    @Override
    @Cacheable(AVAILABLE_ITEMS)
    public Mono<List<ShelfDto>> findAllAvailableItems() {
        return shelfRepository.findByQuantityAbove(0)
                .map(ShelfDto::from)
                .collectList();
    }
}
