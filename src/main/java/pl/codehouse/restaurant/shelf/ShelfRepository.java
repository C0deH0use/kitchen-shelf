package pl.codehouse.restaurant.shelf;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
interface ShelfRepository extends ReactiveCrudRepository<ShelfEntity, Integer> {
    Mono<ShelfEntity> findByMenuItemId(Integer menuItemId);

    Mono<Boolean> existsByMenuItemId(Integer menuItemId);

    @Query("SELECT * FROM shelf WHERE quantity >= :quantity")
    Flux<ShelfEntity> findByQuantityAbove(@Param("quantity") Integer quantity);
}
