package pl.codehouse.restaurant.shelf;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.webTestClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.data.relational.core.query.Criteria.*;
import static org.springframework.data.relational.core.query.Query.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.codehouse.restaurant.TestcontainersConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.cache.type=NONE"}
)
@AutoConfigureWebTestClient(timeout = "500000000000")
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
class ShelfResourceIntegrationTest {

    private static final int SHELF_ID = 1011;
    private static final String MENU_ITEM_NAME = "Test Menu Item";
    private static final int MENU_ITEM_ID = 1010;
    private static final int MENU_ITEM_QUANTITY = 10;
    private static final int MENU_ITEM_START_VERSION = 1;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    private final static Clock clock = Clock.fixed(Instant.parse("2025-01-22T10:15:30.00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        webTestClient(webTestClient);

        flyway.clean();
        flyway.migrate();

        // Insert test data
        r2dbcEntityTemplate.delete(ShelfEntity.class).from("shelf").all().block();

        ShelfEntity testShelfEntity = new ShelfEntity(SHELF_ID, MENU_ITEM_NAME, MENU_ITEM_ID, MENU_ITEM_QUANTITY, MENU_ITEM_START_VERSION, LocalDateTime.now(clock));
        r2dbcEntityTemplate.insert(ShelfEntity.class)
                .using(testShelfEntity)
                .doOnNext(shelf -> System.out.println("Added new shelf item " + shelf.toString()))
                .block();

        List<ShelfEntity> shelfEntities = r2dbcEntityTemplate.select(ShelfEntity.class).from("shelf").all().collectList().block();
        Assertions.assertThat(shelfEntities)
                .hasSize(1)
                .allSatisfy(entity -> assertThat(entity)
                        .hasFieldOrPropertyWithValue("id", SHELF_ID)
                        .hasFieldOrPropertyWithValue("menuItemId", MENU_ITEM_ID)
                        .hasFieldOrPropertyWithValue("menuItemName", MENU_ITEM_NAME)
                        .hasFieldOrPropertyWithValue("quantity", MENU_ITEM_QUANTITY)
                        .hasFieldOrPropertyWithValue("version", Long.valueOf(MENU_ITEM_START_VERSION))
                );
    }

    @Test
    void testAddNewItemToShelf() {
        String menuItemName = "New Item";
        int menuItemId = 2;
        int quantity = 5;
        int expectedVersion = MENU_ITEM_START_VERSION;

        var newItemRequest = Map.of(
                "menuItemId", menuItemId,
                "menuItemName", menuItemName,
                "quantity", quantity
        );

        given()
                .contentType(APPLICATION_PROBLEM_JSON_VALUE)
                .body(newItemRequest)

                .when()
                .post("/shelf")

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.CREATED)
                .body("menuItemId", equalTo(menuItemId))
                .body("menuItemName", equalTo(menuItemName))
                .body("quantity", equalTo(quantity))
                .body("version", equalTo(expectedVersion));

        // and
        Mono<ShelfEntity> addedItem = r2dbcEntityTemplate.select(ShelfEntity.class)
                .matching(query(where("menuItemId").is(menuItemId)))
                .one();

        StepVerifier.create(addedItem)
                .assertNext(newShelf -> assertThat(newShelf)
                        .hasFieldOrPropertyWithValue("id", 1000)
                        .hasFieldOrPropertyWithValue("menuItemId", menuItemId)
                        .hasFieldOrPropertyWithValue("menuItemName", menuItemName)
                        .hasFieldOrPropertyWithValue("quantity", quantity)
                        .hasFieldOrPropertyWithValue("version", Long.valueOf(expectedVersion))
                        .hasFieldOrProperty("updatedAt")
                )
                .verifyComplete();
    }

    @Test
    void should_return400_When_requestIsInvalid() {
        String menuItemName = StringUtils.SPACE;
        int menuItemId = 2;
        int quantity = 5;

        var newItemRequest = Map.of(
                "menuItemId", menuItemId,
                "menuItemName", menuItemName,
                "quantity", quantity
        );

        given()
                .contentType(APPLICATION_PROBLEM_JSON_VALUE)
                .body(newItemRequest)

                .when()
                .post("/shelf")

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.BAD_REQUEST)
                .body("instance", equalTo("/shelf"))
                .body("status", equalTo(BAD_REQUEST.value()))
                .body("detail", equalTo("Invalid request content."))
                .body("errors", hasSize(1))
                .body("errors[0].field", equalTo("menuItemName"))
                .body("errors[0].rejectedValue", equalTo(StringUtils.SPACE))
                .body("errors[0].defaultMessage", equalTo("must not be blank"))
                .body("errors[0].codes", hasItems("NotBlank"))
        ;
    }

    @Test
    void testUpdateByMenuItem() {
        int quantity = 5;
        int expectedQuantity = MENU_ITEM_QUANTITY + quantity;
        int expectedVersion = 2;

        var updateRequest = Map.of(
                "updateType", "ADD",
                "quantity", quantity
        );

        given()
                .log().ifValidationFails()
                .contentType(APPLICATION_PROBLEM_JSON_VALUE)
                .body(updateRequest)

        .when()
                .put("/shelf/"+ MENU_ITEM_ID)

        .then()
                .log().ifValidationFails()
                .status(OK)
                .body("menuItemId", equalTo(MENU_ITEM_ID))
                .body("menuItemName", equalTo(MENU_ITEM_NAME))
                .body("quantity", equalTo(expectedQuantity))
                .body("version", equalTo(expectedVersion));


        // and
        Mono<ShelfEntity> addedItem = r2dbcEntityTemplate.select(ShelfEntity.class)
                .matching(query(
                        where("menuItemId").is(MENU_ITEM_ID)))
                .one();

        StepVerifier.create(addedItem)
                .assertNext(newShelf -> assertThat(newShelf)
                        .hasFieldOrPropertyWithValue("id", SHELF_ID)
                        .hasFieldOrPropertyWithValue("menuItemId", MENU_ITEM_ID)
                        .hasFieldOrPropertyWithValue("menuItemName", MENU_ITEM_NAME)
                        .hasFieldOrPropertyWithValue("quantity", expectedQuantity)
                        .hasFieldOrPropertyWithValue("version", Long.valueOf(expectedVersion))
                        .hasFieldOrProperty("updatedAt")
                )
                .verifyComplete();
    }

    @Test
    void testFetchByMenuItem() {
        given()
            .contentType(APPLICATION_PROBLEM_JSON_VALUE)

        .when()
            .get("/shelf/" + MENU_ITEM_ID)

        .then()
                .log().ifValidationFails()
                .status(OK)
                .body("menuItemId", equalTo(MENU_ITEM_ID))
                .body("menuItemName", equalTo(MENU_ITEM_NAME))
                .body("quantity", equalTo(MENU_ITEM_QUANTITY))
                .body("version", equalTo(MENU_ITEM_START_VERSION));
    }

    @Test
    void testFindAllAvailableItems() {
        given()
                .contentType(APPLICATION_PROBLEM_JSON_VALUE)

                .when()
                .get("/shelf")

                .then()
                .log().ifValidationFails()
                .status(OK)
                .body("$", hasSize(1))
                .body("[0].menuItemId", equalTo(MENU_ITEM_ID))
                .body("[0].menuItemName", equalTo(MENU_ITEM_NAME))
                .body("[0].quantity", equalTo(MENU_ITEM_QUANTITY))
                .body("[0].version", equalTo(MENU_ITEM_START_VERSION));
    }
}
