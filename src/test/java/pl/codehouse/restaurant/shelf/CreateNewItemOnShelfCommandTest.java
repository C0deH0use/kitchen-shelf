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
import pl.codehouse.restaurant.ExecutionResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CreateNewItemOnShelfCommandTest {

    private static final int MENU_ITEM_ID_ONE = 1000;
    private static final String MENU_ITEM_NAME_ONE = "Menu Item One";

    @InjectMocks
    private CreateNewItemOnShelfCommand sut;

    @Mock
    private ShelfRepository repository;

    @Captor
    private ArgumentCaptor<ShelfEntity> entityArgumentCaptor;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2025-01-03T10:15:30.00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("should create new item on shelf when Create Action is passed and item doesn't exist")
    void should_CreateNewItemOnShelf_When_CreateActionIsPassedAndItemDoesntExist() {
        // given
        ShelfAction action = new CreateNewItemOnShelfAction(MENU_ITEM_ID_ONE, MENU_ITEM_NAME_ONE, 5);
        given(repository.existsByMenuItemId(MENU_ITEM_ID_ONE)).willReturn(Mono.just(false));
        given(repository.save(any())).willAnswer(invocation -> Mono.just(invocation.getArguments()[0]));

        // when
        Mono<ExecutionResult<ShelfDto>> resultMono = sut.execute(action);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.handle())
                            .hasFieldOrPropertyWithValue("menuItemId", MENU_ITEM_ID_ONE)
                            .hasFieldOrPropertyWithValue("menuItemName", MENU_ITEM_NAME_ONE)
                            .hasFieldOrPropertyWithValue("quantity", 5)
                            .hasFieldOrPropertyWithValue("version", 0L);
                })
                .verifyComplete();

        // and
        then(repository).should(times(1)).save(entityArgumentCaptor.capture());

        assertThat(entityArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("id", 0)
                .hasFieldOrPropertyWithValue("menuItemId", MENU_ITEM_ID_ONE)
                .hasFieldOrPropertyWithValue("menuItemName", MENU_ITEM_NAME_ONE)
                .hasFieldOrPropertyWithValue("quantity", 5)
                .hasFieldOrPropertyWithValue("version", 0L)
                .hasFieldOrPropertyWithValue("updatedAt", LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("should fail to create new item on shelf when Create Action is passed and item already exists")
    void should_FailToCreateNewItemOnShelf_When_CreateActionIsPassedAndItemAlreadyExists() {
        // given
        ShelfAction action = new CreateNewItemOnShelfAction(MENU_ITEM_ID_ONE, MENU_ITEM_NAME_ONE, 5);
        given(repository.existsByMenuItemId(MENU_ITEM_ID_ONE)).willReturn(Mono.just(true));

        // when
        Mono<ExecutionResult<ShelfDto>> resultMono = sut.execute(action);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isFailure()).isTrue();
                    assertThat(result.exception())
                            .isInstanceOf(RuntimeException.class)
                            .hasCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage("Following menu item already exists %s on shelf".formatted(MENU_ITEM_ID_ONE));
                })
                .verifyComplete();

        // and
        then(repository).should(never()).save(any());
    }
}
