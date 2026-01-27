package API.MiddleTest.Iteration1_Middle;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositTests {

    private static final String ERROR_INVALID_ID_DATA = "Unauthorized access to account";
    private static final String ERROR_INVALID_SUM = "Invalid account or amount";
    private static final String ERROR_JSON_ERROR = "Internal Server Error";

    private static final String user1Token = "Qm9nZGFuMjAwMjpCb2dkYW5pb18lMTIzNDVx";
    private static final String DEPOSIT_URL = "http://localhost:55002/api/v1/accounts/deposit";
    private static final String PROFILE_URL = "http://localhost:55002/api/v1/customer/profile";


    private double getCurrentBalance(int accountId) {
        return given()
                .header("Authorization", "Bearer " + user1Token)
                .when()
                .get(PROFILE_URL)
                .then()
                .statusCode(200)
                .extract()
                .path("balance");
    }

    // ---------------------- VALID DEPOSIT ----------------------

    public static Stream<Arguments> dataForValidDeposit() {
        return Stream.of(
                Arguments.of(1, 100.00),
                Arguments.of(1, 4999.99),
                Arguments.of(1, 0.01)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForValidDeposit")
    @DisplayName("Deposit with valid values should increase balance")
    public void testValidDeposit(int accountId, double amount) {

        double oldBalance = getCurrentBalance(accountId);

        String requestBody = """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """.formatted(accountId, amount);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(requestBody)
                .when()
                .post(DEPOSIT_URL)
                .then()
                .statusCode(200)
                .body("id", equalTo(accountId));

        double newBalance = getCurrentBalance(accountId);

        assertEquals(oldBalance + amount, newBalance, 0.01);
    }

    // ---------------------- INVALID SUMS ----------------------

    public static Stream<Arguments> dataForInvalidSums() {
        return Stream.of(
                Arguments.of(1, 0),
                Arguments.of(1, -100),
                Arguments.of(1, 5000.01),
                Arguments.of(1, 99999)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidSums")
    @DisplayName("Invalid deposit amounts should NOT change balance")
    public void testInvalidSums(int accountId, double amount) {

        double oldBalance = getCurrentBalance(accountId);

        String requestBody = """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """.formatted(accountId, amount);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(requestBody)
                .when()
                .post(DEPOSIT_URL)
                .then()
                .statusCode(400)
                .body("message", equalTo(ERROR_INVALID_SUM));

        double newBalance = getCurrentBalance(accountId);

        assertEquals(oldBalance, newBalance);
    }

    // ---------------------- INVALID ACCOUNT ID ----------------------

    public static Stream<Arguments> invalidAccountIds() {
        return Stream.of(
                Arguments.of(909, ERROR_INVALID_ID_DATA),
                Arguments.of(3, ERROR_INVALID_ID_DATA)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAccountIds")
    @DisplayName("Invalid account ID should not change balance")
    public void testInvalidAccountId(int accountId, String errorMessage) {

        double oldBalance = getCurrentBalance(1); // user1 real account

        String requestBody = """
                {
                  "id": %d,
                  "balance": 300
                }
                """.formatted(accountId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(requestBody)
                .when()
                .post(DEPOSIT_URL)
                .then()
                .statusCode(403)
                .body("message", equalTo(errorMessage));

        double newBalance = getCurrentBalance(1);

        assertEquals(oldBalance, newBalance);
    }

    // ---------------------- INVALID JSON ----------------------

    public static Stream<Arguments> invalidJson() {
        return Stream.of(
                Arguments.of("{id:1, balance:100}"),
                Arguments.of("{\"id\":1 \"balance\":100}"),
                Arguments.of("not a json at all")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJson")
    @DisplayName("Invalid JSON should return server error and not change balance")
    public void testInvalidJson(String rawJson) {

        double oldBalance = getCurrentBalance(1);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user1Token)
                .body(rawJson)
                .when()
                .post(DEPOSIT_URL)
                .then()
                .statusCode(500)
                .body("message", equalTo(ERROR_JSON_ERROR));

        double newBalance = getCurrentBalance(1);

        assertEquals(oldBalance, newBalance);
    }

    // ---------------------- NO AUTH ----------------------

    @Test
    @DisplayName("Deposit without auth should return 400 and not change balance")
    public void testDepositWithoutAuth() {

        double oldBalance = getCurrentBalance(1);

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
                .post(DEPOSIT_URL)
                .then()
                .statusCode(400)
                .body("message", equalTo(ERROR_INVALID_ID_DATA));

        double newBalance = getCurrentBalance(1);

        assertEquals(oldBalance, newBalance);
    }
}
