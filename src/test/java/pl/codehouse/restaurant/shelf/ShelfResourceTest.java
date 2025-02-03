package pl.codehouse.restaurant.shelf;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.webTestClient;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
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
        Locale.setDefault(ENGLISH);
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
                    .status(OK)
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
                    .status(OK)
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
                    .body("title", equalTo( "Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("instance", equalTo("/shelf/-1000"))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("fetchByMenuItem.menuItemId"))
                    .body("errors[0].objectName", equalTo("fetchByMenuItem.menuItemId"))
                    .body("errors[0].rejectedValue", equalTo(-1000))
                    .body("errors[0].defaultMessage", equalTo("must be greater than 0"))
                    .body("errors[0].codes", hasItems("{jakarta.validation.constraints.Positive.message}"))
                    ;
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
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("detail", equalTo("Type mismatch."))
                    .body("instance", equalTo("/shelf/abc"))
            ;
        }
    }

    @Nested
    @DisplayName("Update given MenuItems on shelf")
    class UpdateShelfByMenuItemId {
        @Test
        @DisplayName("should successfully update a menu item on the shelf")
        void Should_SuccessfullyUpdateMenuItemOnShelf() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "updateType", UpdateType.ADD.name(),
                    "quantity", 5
            );
            UpdateItemOnShelfAction action = new UpdateItemOnShelfAction(MENU_ITEM_ID, UpdateType.ADD, 5);
            ShelfDto expectedShelfDto = new ShelfDto("Updated Item", MENU_ITEM_ID, 10, 2);
            given(shelfService.action(action)).willReturn(Mono.just(expectedShelfDto));

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .put("/shelf/1000")

                    .then()
                    .log().ifValidationFails()
                    .status(OK)
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
                    .put("/shelf/{menuItemId}", Integer.toString(MENU_ITEM_ID))

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("instance", equalTo("/shelf/1000"))
                    .body("detail", equalTo("Failed to read HTTP message"))
            ;
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
                    .put("/shelf/{menuItemId}", Integer.toString(MENU_ITEM_ID))

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("updateType"))
                    .body("errors[0].rejectedValue", equalTo(null))
                    .body("errors[0].defaultMessage", equalTo("must not be null"))
                    .body("errors[0].codes", hasItems("NotNull", "NotNull.updateType"))

            ;
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
                    .put("/shelf/{menuItemId}", Integer.toString(MENU_ITEM_ID))

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("quantity"))
                    .body("errors[0].rejectedValue", equalTo(0))
                    .body("errors[0].defaultMessage", equalTo("must be greater than or equal to 1"))
                    .body("errors[0].codes", hasItems("Min", "Min.int", "Min.quantity"))
            ;
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
                    .put("/shelf/-1")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("updateByMenuItem.menuItemId"))
                    .body("errors[0].rejectedValue", equalTo(-1))
                    .body("errors[0].defaultMessage", equalTo("must be greater than 0"))
                    .body("errors[0].codes", hasItems("{jakarta.validation.constraints.Positive.message}"))
            ;
        }
    }

    @Nested
    @DisplayName("Add given MenuItem on shelf")
    class AddNewItemToShelf {
        @Test
        @DisplayName("should successfully add a new item to the shelf")
        void Should_SuccessfullyAddNewItemToShelf() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "menuItemId", 1,
                    "menuItemName", "New Item",
                    "quantity", 5
            );
            ShelfDto expectedShelfDto = new ShelfDto("New Item", 1, 5, 1);
            CreateNewItemOnShelfAction action = new CreateNewItemOnShelfAction(1, "New Item", 5);
            given(shelfService.action(action)).willReturn(Mono.just(expectedShelfDto));

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(CREATED)
                    .body("menuItemName", equalTo("New Item"))
                    .body("menuItemId", equalTo(1))
                    .body("quantity", equalTo(5))
                    .body("version", equalTo(1));
        }

        @Test
        @DisplayName("should return 400 with detailed error when adding a new item with invalid menu item id")
        void Should_Return400WithDetailedError_When_AddingNewItemWithInvalidMenuItemId() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "menuItemId", -1,
                    "menuItemName", "Invalid Item",
                    "quantity", 5
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("menuItemId"))
                    .body("errors[0].rejectedValue", equalTo(-1))
                    .body("errors[0].defaultMessage", equalTo("must be greater than or equal to 1"))
                    .body("errors[0].codes", hasItems("Min.int", "Min"))
            ;
        }

        @Test
        @DisplayName("should return 400 when adding a new item with blank menu item name")
        void Should_Return400_When_AddingNewItemWithBlankMenuItemName() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "menuItemId", 1,
                    "menuItemName", StringUtils.EMPTY,
                    "quantity", 5
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("menuItemName"))
                    .body("errors[0].rejectedValue", equalTo(StringUtils.EMPTY))
                    .body("errors[0].defaultMessage", equalTo("must not be blank"))
                    .body("errors[0].codes", hasItems("NotBlank"))
            ;
        }

        @Test
        @DisplayName("should return 400 when adding a new item with invalid quantity")
        void Should_Return400_When_AddingNewItemWithInvalidQuantity() {
            // given
            Map<String, Object> requestBody = Map.of(
                    "menuItemId", 1,
                    "menuItemName", "Invalid Quantity Item",
                    "quantity", 0
            );

            given()
                    .log().ifValidationFails()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)

                    .when()
                    .post("/shelf")

                    .then()
                    .log().ifValidationFails()
                    .status(BAD_REQUEST)
                    .body("title", equalTo("Bad Request"))
                    .body("status", equalTo(BAD_REQUEST.value()))
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("quantity"))
                    .body("errors[0].rejectedValue", equalTo(0))
                    .body("errors[0].defaultMessage", equalTo("must be greater than or equal to 1"))
                    .body("errors[0].codes", hasItems("Min.quantity", "Min"));
        }
    }
}
