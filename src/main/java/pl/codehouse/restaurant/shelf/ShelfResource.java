package pl.codehouse.restaurant.shelf;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/shelf", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
class ShelfResource {
    private final ShelfQueryService queryService;

    ShelfResource(ShelfQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    Mono<List<ShelfDto>> fetchAvailableItemsOnShelf() {
        return queryService.findAllAvailableItems();
    }

    @GetMapping("/{menuItemId}")
    Mono<ShelfDto> fetchByMenuItem(@PathVariable @NotNull Integer menuItemId) {
        return queryService.findByMenuItemId(menuItemId);
    }
}
