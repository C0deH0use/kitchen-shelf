package pl.codehouse.restaurant.shelf;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.codehouse.restaurant.Command;
import pl.codehouse.restaurant.ExecutionResult;
import reactor.core.publisher.Mono;

@Component
class CreateNewItemOnShelfCommand implements Command<ShelfAction, ShelfDto> {
    private static final Logger log = LoggerFactory.getLogger(CreateNewItemOnShelfCommand.class);
    private static final int NEW_VERSION = 1;

    private final Clock clock;
    private final ShelfRepository repository;

    CreateNewItemOnShelfCommand(Clock clock, ShelfRepository repository) {
        this.clock = clock;
        this.repository = repository;
    }

    @Override
    public boolean isApplicable(ShelfAction t) {
        return t instanceof CreateNewItemOnShelfAction;
    }

    @Override
    public Mono<ExecutionResult<ShelfDto>> execute(ShelfAction context) {
        var input = (CreateNewItemOnShelfAction) context;
        return repository.existsByMenuItemId(input.menuItemId())
                .flatMap(handleIfMenuItemExists(input))
                .map(empty -> createEntity(input))
                .doOnNext(entity -> log.info("Storing entity after Action >>> {}", entity))
                .flatMap(repository::save)
                .map(ShelfDto::from)
                .map(ExecutionResult::success)
                .onErrorResume((error) -> {
                    log.error("Error thrown during execution of Create action on {}. Error Message: {}",
                              input.menuItemId(), error.getMessage(), error);
                    return Mono.just(ExecutionResult.failure(new RuntimeException(error)));
                });
    }

    private static Function<Boolean, Mono<? extends Boolean>> handleIfMenuItemExists(CreateNewItemOnShelfAction input) {
        return menuItemExists -> {
            if (menuItemExists) {
                return Mono.error(new IllegalStateException("Following menu item already exists %s on shelf".formatted(input.menuItemId())));
            }
            return Mono.just(true);
        };
    }

    private ShelfEntity createEntity(CreateNewItemOnShelfAction input) {
        var menuItemId = input.menuItemId();
        var menuItemName = input.menuItemName();
        var newQuantity = input.quantity();
        var updateAt = LocalDateTime.now(clock);

        log.info("Performing `Create` Action on entity: {} >>> Adding {} items", menuItemId, newQuantity);
        return new ShelfEntity(0, menuItemName, menuItemId, newQuantity, NEW_VERSION, updateAt);
    }
}
