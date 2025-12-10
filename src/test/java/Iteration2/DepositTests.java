package Iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class DepositTests {
    private static final String ERROR_INVALID_ID_DATA = "Unauthorized access to account";
    private static final String ERROR_FOR_INVALID_JSON_FORMAT_AND_MISSING_DATA_ERROR = "Internal Server Error";
    private static final String ERROR_INVALID_SUM = "Invalid account or amount";
    private static final String user1Token = "Qm9nZGFuMjAwMjpCb2dkYW5pb18lMTIzNDVx";
    private final static String URL ="http://localhost:55002/api/v1/accounts/deposit";




    public static Stream<Arguments> dataForValidDeposit() {
        return Stream.of(
                Arguments.of(1, 100),
                Arguments.of(1, 4999.99),
                Arguments.of(1, 0.01)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForValidDeposit")
    @DisplayName("Deposit with different valid values")
    public void userInvalidDepSums(int accountId, double amount) {

        String requestBody = """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """.formatted(accountId, amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(accountId))
                .body("balance", greaterThan(amount));
    }

    public static Stream<Arguments> dataForNegativeTests() {
        return Stream.of(
                Arguments.of(1, 100),
                Arguments.of(1, 5000.01),
                Arguments.of(1, 0),
                Arguments.of(1, 600),
                Arguments.of(1, -100)

        );
    }

    @ParameterizedTest
    @MethodSource("dataForNegativeTests")
    @DisplayName("Deposit with different negative values")
    public void userPositiveDepositWithDifferentSums(int accountId, double amount) {
        String requestBody = """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """.formatted(accountId, amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo(ERROR_INVALID_SUM));


    }

    public static Stream<Arguments> dataForNegativeValidationTest() {
        return Stream.of(
                Arguments.of(909, ERROR_INVALID_ID_DATA),//invalid account id
                Arguments.of(3, ERROR_INVALID_ID_DATA)//account id that belongs to other user


        );
    }

    @ParameterizedTest
    @MethodSource("dataForNegativeValidationTest")
    @DisplayName("ValidationCaseForIDField")
    public void ValidationsForFieldsData(int accountId, String errorMessage) {


        String requestBody = """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """.formatted(accountId, 300);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .assertThat()
                .statusCode(403)
                .body("message", equalTo(errorMessage));
    }

    public static Stream<Arguments> invalidJsonSyntaxData() {
        return Stream.of(
                Arguments.of("{id:1, balance:100}"),                      // missing quotes
                Arguments.of("{\"id\":1 \"balance\":100}"),               // missing comma
                Arguments.of("not a json at all")                         // plain invalid string
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJsonSyntaxData")
    @DisplayName("Invalid JSON Syntax Errors")
    public void invalidJsonSyntaxTests(String rawJson) {

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(rawJson)
                .when()
                .post(URL)
                .then()
                .assertThat()
                .statusCode(500)
                .body("message", equalTo(ERROR_FOR_INVALID_JSON_FORMAT_AND_MISSING_DATA_ERROR));
    }

    @Test
    @DisplayName("Deposit Try Without Auth")
    public void depositTryWithoutAuth() {

        String requestBody = """
                {
                  "id": 1,
                  "balance": 300
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo(ERROR_INVALID_ID_DATA));
    }

    public static Stream<Arguments> missingOrNullFieldsData() {
        return Stream.of(
                // missing fields
                Arguments.of(
                        """
                                {
                                  "balance": 100
                                }
                                """,

                        "Missing required field: id"
                ),
                Arguments.of(
                        """
                                {
                                  "id": 1
                                }
                                """,

                        "Missing required field: balance"
                ),

                // null values
                Arguments.of(
                        """
                                {
                                  "id": null,
                                  "balance": 100
                                }
                                """,

                        "Invalid account ID"
                ),
                Arguments.of(
                        """
                                {
                                  "id": 1,
                                  "balance": null
                                }
                                """,

                        "Amount must be greater than zero"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("missingOrNullFieldsData")
    @DisplayName("Negative: missing or null fields in deposit request")
    public void negativeMissingOrNullFields(String rawJson) {

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic " + user1Token)
                .body(rawJson)
                .when()
                .post(URL)
                .then()
                .assertThat()
                .statusCode(500)
                .body("message", equalTo(ERROR_FOR_INVALID_JSON_FORMAT_AND_MISSING_DATA_ERROR));
    }



}


