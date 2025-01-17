package pl.codehouse.restaurant.shelf;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import pl.codehouse.restaurant.Command;
import pl.codehouse.restaurant.ExecutionResult;
import reactor.core.publisher.Mono;

@Component
class UpdateItemOnShelfCommand implements Command<ShelfAction, ShelfDto> {
    private static final Logger log = LoggerFactory.getLogger(UpdateItemOnShelfCommand.class);
    private static final String ITEMS_BY_MENU_ITEM_ID = "itemsByMenuItemId";

    private final Clock clock;
    private final ShelfRepository repository;
    private final CacheManager cacheManager;

    UpdateItemOnShelfCommand(Clock clock, ShelfRepository repository, CacheManager cacheManager) {
        this.clock = clock;
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean isApplicable(ShelfAction t) {
        return t instanceof UpdateItemOnShelfAction;
    }

    @Override
    public Mono<ExecutionResult<ShelfDto>> execute(ShelfAction context) {
        var input = (UpdateItemOnShelfAction) context;
        return repository.findByMenuItemId(input.menuItemId())
                .flatMap(validateMenuItemExists(input))
                .map(entity -> performAction(entity, input))
                .doOnNext(entity -> log.info("Storing entity after Action >>> {}", entity))
                .flatMap(repository::save)
                .doOnNext(handleCacheUpdate())
                .map(ShelfDto::from)
                .map(ExecutionResult::success)
                .onErrorResume((error) -> {
                    log.error("Error thrown during execution of Update Type action {} on {}. Error Message: {}",
                              input.updateType(), input.menuItemId(), error.getMessage(), error);
                    return Mono.just(ExecutionResult.failure(new RuntimeException(error)));
                });
    }

    private Consumer<ShelfEntity> handleCacheUpdate() {
        return entity -> {
            Cache cache = cacheManager.getCache(ITEMS_BY_MENU_ITEM_ID);
            if (cache == null) {
                return;
            }
            cache.put(entity.menuItemId(), entity);
        };
    }

    private Function<ShelfEntity, Mono<? extends ShelfEntity>> validateMenuItemExists(UpdateItemOnShelfAction input) {
        return entity -> {
            boolean actionApplicable = isActionApplicable(entity, input.updateType(), input.quantity());
            if (!actionApplicable) {
                int remainingItems = input.quantity() - entity.quantity();
                return Mono.error(new IllegalStateException("Missing %d item(s) of %s from shelf".formatted(remainingItems, input.menuItemId())));
            }
            return Mono.just(entity);
        };
    }

    private ShelfEntity performAction(ShelfEntity entity, UpdateItemOnShelfAction input) {
        return switch (input.updateType()) {
            case ADD -> {
                var newQuantity = entity.quantity() + input.quantity();
                var newVersion = entity.version() + 1;
                var updateAt = LocalDateTime.now(clock);

                log.info("Performing `Add` Action on entity: {} >>> Adding {} items", entity.menuItemId(), newQuantity);
                yield new ShelfEntity(entity.id(), entity.menuItemName(), entity.menuItemId(), newQuantity, newVersion, updateAt);
            }
            case TAKE -> {
                var newQuantity = entity.quantity() - input.quantity();
                var newVersion = entity.version() + 1;
                var updateAt = LocalDateTime.now(clock);

                log.info("Performing `Take` Action on entity: {} >>> Adding {} items", entity.menuItemId(), newQuantity);
                yield new ShelfEntity(entity.id(), entity.menuItemName(), entity.menuItemId(), newQuantity, newVersion, updateAt);
            }
            case null -> entity;
        };
    }

    private boolean isActionApplicable(ShelfEntity entity, UpdateType updateType, int quantity) {
        log.info("Checking if {} action is applicable on shelf entity: {}", updateType, entity);
        if (updateType == UpdateType.ADD) {
            return true;
        }

        return entity.quantity() >= quantity;
    }
}
