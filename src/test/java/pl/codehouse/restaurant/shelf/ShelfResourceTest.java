package pl.codehouse.restaurant.shelf;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.webTestClient;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(ShelfResource.class)
@AutoConfigureWebTestClient(timeout = "500000000000")
class ShelfResourceTest {

    private static final int MENU_ITEM_ID = 1000;
    private static final int MENU_ITEM_ID_TWO = 1001;
    @MockitoBean
    private ShelfService shelfService;

    @MockitoBean
    private ShelfQueryService shelfQueryService;

    @BeforeEach
    void setUp(@Autowired WebTestClient webTestClient) {
        webTestClient(webTestClient);
    }

    @Nested
    @DisplayName("Fetch all shelf details that are available")
    class GetShelf {
        @Test
        @DisplayName("should return available shelf items")
        void shouldReturnAvailableShelfItems() {
            // given
            List<ShelfDto> expectedShelfMenuItems = List.of(
                    new ShelfDto("menuItemName One", MENU_ITEM_ID, 4, 1),
                    new ShelfDto("menuItemName Two", MENU_ITEM_ID_TWO, 5, 2)
            );
            given(shelfQueryService.findAllAvailableItems()).willReturn(Mono.just(expectedShelfMenuItems));

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)

                    .when()
                    .get("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(HttpStatus.OK)
                    .body("$", Matchers.hasSize(2))
                    .body("$.menuItemName", notNullValue())
                    .body("$.menuItemId", notNullValue())
                    .body("$.quantity", notNullValue())
                    .body("$.version", notNullValue());
        }
    }

    @Nested
    @DisplayName("Fetch shelf details by menuItem")
    class GetShelfByMenuItemId {
        @Test
        @DisplayName("should return shelf details when requesting by menu item id")
        void shouldReturnShelfDetailsWhenRequestingByMenuItemId() {
            // given
            ShelfDto shelfDto = new ShelfDto("menuItemName One", MENU_ITEM_ID, 4, 1);
            given(shelfQueryService.findByMenuItemId(MENU_ITEM_ID)).willReturn(Mono.just(shelfDto));

            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .log().ifValidationFails()

                    .when()
                    .get("/shelf/{menuItemId}", Integer.toString(MENU_ITEM_ID))

                    .then()
                    .log().ifValidationFails()
                    .status(HttpStatus.OK)
                    .body("menuItemName", notNullValue())
                    .body("menuItemId", notNullValue())
                    .body("quantity", notNullValue())
                    .body("version", notNullValue());
        }
        
        @Test
        @DisplayName("should return 400 when requesting with invalid menu item value")
        void shouldReturn400WhenRequestingWithInvalidMenuItemValue() {
            // given
            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)

                    .when()
                    .get("/shelf/{menuItemId}", "-1000")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf/-1000"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }

        @Test
        @DisplayName("should return 400 when requesting with invalid menu item type")
        void shouldReturn400WhenRequestingWithInvalidMenuItemType() {
            // given
            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)

                    .when()
                    .get("/shelf/{menuItemId}", "abc")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf/abc"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }
    }

    @Nested
    @DisplayName("Update given MenuItems on shelf")
    class UpdateShelfByMenuItemId {
        @Test
        @DisplayName("should successfully update a menu item on the shelf")
        void Should_SuccessfullyUpdateMenuItemOnShelf() {
            // given
            UpdateMenuItemOnShelfRequest request = new UpdateMenuItemOnShelfRequest(UpdateType.ADD, 5);
            UpdateItemOnShelfAction action = new UpdateItemOnShelfAction(MENU_ITEM_ID, UpdateType.ADD, 5);
            ShelfDto expectedShelfDto = new ShelfDto("Updated Item", MENU_ITEM_ID, 10, 2);
            given(shelfService.action(action)).willReturn(Mono.just(expectedShelfDto));

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)

                    .when()
                    .put("/shelf/{menuItemId}", MENU_ITEM_ID)

                    .then()
                    .log().ifValidationFails()
                    .status(HttpStatus.OK)
                    .body("menuItemName", equalTo("Updated Item"))
                    .body("menuItemId", equalTo(MENU_ITEM_ID))
                    .body("quantity", equalTo(10))
                    .body("version", equalTo(2));
        }

        @Test
        @DisplayName("should return 400 when calling updateByMenuItem with invalid updateType in the payload")
        void Should_Return400_When_CallingUpdateByMenuItemWithInvalidUpdateTypeInThePayload() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "updateType", "INVALID",
                    "quantity", 5
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .put("/shelf/{menuItemId}", MENU_ITEM_ID)

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf/" + MENU_ITEM_ID))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }

        @Test
        @DisplayName("should return 400 when calling updateByMenuItem with missing updateType in the payload")
        void Should_Return400_When_CallingUpdateByMenuItemWithMissingUpdateTypeInThePayload() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "quantity", 5
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .put("/shelf/{menuItemId}", MENU_ITEM_ID)

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf/" + MENU_ITEM_ID))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }

        @Test
        @DisplayName("should return 400 when calling updateByMenuItem with missing quantity in the payload")
        void Should_Return400_When_CallingUpdateByMenuItemWithMissingQuantityInThePayload() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "updateType", "ADD"
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .put("/shelf/{menuItemId}", MENU_ITEM_ID)

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf/" + MENU_ITEM_ID))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }

        @Test
        @DisplayName("should return 400 When calling updateByMenuItem with incorrect menu item id path variable")
        void Should_Return400_When_CallingUpdateByMenuItemWithIncorrectMenuItemIdPathVariable() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "updateType", "ADD",
                    "quantity", 5
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .put("/shelf/{menuItemId}", "-1")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf/-1"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }
    }

    @Nested
    @DisplayName("Add given MenuItem on shelf")
    class AddNewItemToShelf {
        @Test
        @DisplayName("should successfully add a new item to the shelf")
        void Should_SuccessfullyAddNewItemToShelf() {
            // given
            CreateNewItemOnShelfAction action = new CreateNewItemOnShelfAction(1, "New Item", 5);
            ShelfDto expectedShelfDto = new ShelfDto("New Item", 1, 5, 1);
            given(shelfService.action(action)).willReturn(Mono.just(expectedShelfDto));

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(action)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(HttpStatus.OK)
                    .body("menuItemName", equalTo("New Item"))
                    .body("menuItemId", equalTo(1))
                    .body("quantity", equalTo(5))
                    .body("version", equalTo(1));
        }

        @Test
        @DisplayName("should return 400 when adding a new item with invalid menu item id")
        void Should_Return400_When_AddingNewItemWithInvalidMenuItemId() {
            // given
            CreateNewItemOnShelfAction action = new CreateNewItemOnShelfAction(-1, "Invalid Item", 5);

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(action)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }

        @Test
        @DisplayName("should return 400 when adding a new item with blank menu item name")
        void Should_Return400_When_AddingNewItemWithBlankMenuItemName() {
            // given
            CreateNewItemOnShelfAction action = new CreateNewItemOnShelfAction(1, "", 5);

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(action)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }

        @Test
        @DisplayName("should return 400 when adding a new item with invalid quantity")
        void Should_Return400_When_AddingNewItemWithInvalidQuantity() {
            // given
            CreateNewItemOnShelfAction action = new CreateNewItemOnShelfAction(1, "Invalid Quantity Item", 0);

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(action)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("timestamp", notNullValue(LocalDateTime.class))
                    .body("requestId", notNullValue(String.class))
                    .body("path", equalTo("/shelf"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("error", hasSize(1));
        }
    }
}
