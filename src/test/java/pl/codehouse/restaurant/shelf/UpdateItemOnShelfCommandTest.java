package pl.codehouse.restaurant.shelf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import pl.codehouse.restaurant.ExecutionResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UpdateItemOnShelfCommandTest {

    private static final int MENU_ITEM_ID_ONE = 1000;
    private static final String MENU_ITEM_NAME_ONE = "Menu Item One";
    private static final String ITEMS_BY_MENU_ITEM_ID = "itemsByMenuItemId";

    @InjectMocks
    private UpdateItemOnShelfCommand sut;

    @Mock
    private ShelfRepository repository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Captor
    private ArgumentCaptor<ShelfEntity> entityArgumentCaptor;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2025-01-03T10:15:30.00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("should add two of a menu item to shelf when Update Action with Add Type is passed")
    void should_AddTwoOfAMenuItemToShelf_When_UpdateActionWithAddTypeIsPassed() {
        // given
        ShelfAction action = new UpdateItemOnShelfAction(MENU_ITEM_ID_ONE, UpdateType.ADD, 2);
        ShelfEntity entity = new ShelfEntity(100, MENU_ITEM_NAME_ONE, MENU_ITEM_ID_ONE, 0, 1, LocalDateTime.now(clock).minusDays(1));
        ShelfEntity expectedUpdatedEntity = new ShelfEntity(100, MENU_ITEM_NAME_ONE, MENU_ITEM_ID_ONE, 2, 2, LocalDateTime.now(clock));
        given(repository.findByMenuItemId(MENU_ITEM_ID_ONE)).willReturn(Mono.just(entity));
        given(repository.save(any())).willAnswer(invocation -> Mono.just(invocation.getArguments()[0]));
        given(cacheManager.getCache(ITEMS_BY_MENU_ITEM_ID)).willReturn(cache);

        // when
        Mono<ExecutionResult<ShelfDto>> resultMono = sut.execute(action);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.handle())
                            .hasFieldOrPropertyWithValue("quantity", 2)
                            .hasFieldOrPropertyWithValue("version", 2L);
                })
                .verifyComplete();

        // and
        then(cache).should(times(1)).put(MENU_ITEM_ID_ONE, expectedUpdatedEntity);

        // and
        then(repository).should(times(1)).save(entityArgumentCaptor.capture());

        assertThat(entityArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("id", 100)
                .hasFieldOrPropertyWithValue("menuItemId", MENU_ITEM_ID_ONE)
                .hasFieldOrPropertyWithValue("menuItemName", MENU_ITEM_NAME_ONE)
                .hasFieldOrPropertyWithValue("quantity", 2)
                .hasFieldOrPropertyWithValue("version", 2L)
                .hasFieldOrPropertyWithValue("updatedAt", LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("should subtract two items from shelf when Update Action with TAKE Type and on shelf are enough items")
    void should_subtractTwoItemsFromShelf_When_UpdateActionWithTAKETypeAndOnShelfAreEnoughItems() {
        // given
        ShelfAction action = new UpdateItemOnShelfAction(MENU_ITEM_ID_ONE, UpdateType.TAKE, 2);
        ShelfEntity entity = new ShelfEntity(100, MENU_ITEM_NAME_ONE, MENU_ITEM_ID_ONE, 10, 1, LocalDateTime.now(clock).minusDays(1));
        ShelfEntity expectedUpdatedEntity = new ShelfEntity(100, MENU_ITEM_NAME_ONE, MENU_ITEM_ID_ONE, 8, 2, LocalDateTime.now(clock));
        given(repository.findByMenuItemId(MENU_ITEM_ID_ONE)).willReturn(Mono.just(entity));
        given(repository.save(any())).willAnswer(invocation -> Mono.just(invocation.getArguments()[0]));
        given(cacheManager.getCache(ITEMS_BY_MENU_ITEM_ID)).willReturn(cache);

        // when
        Mono<ExecutionResult<ShelfDto>> resultMono = sut.execute(action);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.handle())
                            .hasFieldOrPropertyWithValue("quantity", 8)
                            .hasFieldOrPropertyWithValue("version", 2L);
                })
                .verifyComplete();

        // and
        then(cache).should(times(1)).put(MENU_ITEM_ID_ONE, expectedUpdatedEntity);

        // and
        then(repository).should(times(1)).save(entityArgumentCaptor.capture());

        assertThat(entityArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("id", 100)
                .hasFieldOrPropertyWithValue("menuItemId", MENU_ITEM_ID_ONE)
                .hasFieldOrPropertyWithValue("menuItemName", MENU_ITEM_NAME_ONE)
                .hasFieldOrPropertyWithValue("quantity", 8)
                .hasFieldOrPropertyWithValue("version", 2L)
                .hasFieldOrPropertyWithValue("updatedAt", LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("should fail to subtract two items from shelf when Update Action with TAKE Type and there are not that many items on the shelf")
    void should_failToSubtractTwoItemsFromShelf_When_UpdateActionWithTAKETypeAndThereAreNotThatManyItems() {
        // given
        ShelfAction action = new UpdateItemOnShelfAction(MENU_ITEM_ID_ONE, UpdateType.TAKE, 2);
        ShelfEntity entity = new ShelfEntity(100, MENU_ITEM_NAME_ONE, MENU_ITEM_ID_ONE, 1, 1, LocalDateTime.now(clock).minusDays(1));
        given(repository.findByMenuItemId(MENU_ITEM_ID_ONE)).willReturn(Mono.just(entity));

        // when
        Mono<ExecutionResult<ShelfDto>> resultMono = sut.execute(action);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isFailure()).isTrue();
                    assertThat(result.exception())
                            .isInstanceOf(RuntimeException.class)
                            .hasCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage("Missing %d item(s) of %s from shelf".formatted(1, MENU_ITEM_ID_ONE));
                })
                .verifyComplete();

        // and
        then(repository).should(never()).save(any());
    }
}