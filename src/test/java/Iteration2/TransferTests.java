package Iteration2;

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

public class TransferTests {

    private static final String userToken = "Qm9nZGFuMjAwMjpCb2dkYW5pb18lMTIzNDVx";
    private static final String VALID_TRANSFER_MESSAGE = "Transfer successful";
    private static final String INVALID_OR_INSUFFICIENT_FUNDS_MESSAGE = "insufficient funds or invalid accounts";
    private static final String INVALID_IDS_MESSAGE = "Unauthorized access to account";
    private static final String PROFILE_URL = "http://localhost:55002/api/v1/customer/profile";
    private static final String URL = "/api/v1/accounts/transfer";

    // ===== helper =====
    private double getCurrentBalance(int accountId) {
        return given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get(PROFILE_URL)
                .then()
                .statusCode(200)
                .extract()
                .path("accounts.find { it.id == %d }.balance".formatted(accountId));
    }

    // ================= POSITIVE =================

    public static Stream<Arguments> dataForValidTransfer() {
        return Stream.of(
                Arguments.of(50.00),
                Arguments.of(0.01),
                Arguments.of(4999.99)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForValidTransfer")
    @DisplayName("Positive transfer test with different sums")
    public void validTransferTest(double amount) {

        double senderBefore = getCurrentBalance(1);
        double receiverBefore = getCurrentBalance(3);

        String requestBody = """
                {
                   "senderAccountId": 1,
                   "receiverAccountId": 3,
                   "amount": %.2f
                }
                """.formatted(amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .statusCode(200)
                .body("receiverAccountId", equalTo(3))
                .body("amount", equalTo(amount))
                .body("senderAccountId", equalTo(1))
                .body("message", equalTo(VALID_TRANSFER_MESSAGE));

        double senderAfter = getCurrentBalance(1);
        double receiverAfter = getCurrentBalance(3);

        assertEquals(senderBefore - amount, senderAfter, 0.01);
        assertEquals(receiverBefore + amount, receiverAfter, 0.01);
    }

    // ================= NEGATIVE SUMS =================

    public static Stream<Arguments> dataForInvalidTransfer() {
        return Stream.of(
                Arguments.of(-50),
                Arguments.of(0),
                Arguments.of(10000),
                Arguments.of(5000.01)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidTransfer")
    @DisplayName("Negative transfer test with invalid sums")
    public void InvalidTransferTest(double amount) {

        double senderBefore = getCurrentBalance(1);
        double receiverBefore = getCurrentBalance(4);

        String requestBody = """
                {
                   "senderAccountId": 1,
                   "receiverAccountId": 4,
                   "amount": %.2f
                }
                """.formatted(amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .statusCode(400)
                .body("Invalid transfer", equalTo(INVALID_OR_INSUFFICIENT_FUNDS_MESSAGE));

        double senderAfter = getCurrentBalance(1);
        double receiverAfter = getCurrentBalance(4);

        assertEquals(senderBefore, senderAfter);
        assertEquals(receiverBefore, receiverAfter);
    }

    // ================= INVALID IDS =================

    public static Stream<Arguments> dataForInvalidParamsTesT() {
        return Stream.of(
                Arguments.of(99, 2),
                Arguments.of(1, 999),
                Arguments.of(0, 2),
                Arguments.of(1, 0),
                Arguments.of(1, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidParamsTesT")
    @DisplayName("Negative transfer tests with invalid account ids")
    public void InvalidTransferTestWithWrongParams(int senderID, int receiverID) {

        double senderBefore = getCurrentBalance(1);
        double receiverBefore = getCurrentBalance(3);

        String requestBody = """
                {
                   "senderAccountId": %d,
                   "receiverAccountId": %d,
                   "amount": 50
                }
                """.formatted(senderID, receiverID);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestBody)
                .when()
                .post(URL)
                .then()
                .statusCode(403)
                .body(equalTo(INVALID_IDS_MESSAGE));

        double senderAfter = getCurrentBalance(1);
        double receiverAfter = getCurrentBalance(3);

        assertEquals(senderBefore, senderAfter);
        assertEquals(receiverBefore, receiverAfter);
    }

    // ================= INVALID JSON =================

    public static Stream<Arguments> invalidJsonData() {
        return Stream.of(
                Arguments.of("{senderAccountId:1 receiverAccountId:3 amount:50}"),
                Arguments.of("{ \"senderAccountId\": 1 \"amount\": 50 }"),
                Arguments.of("not a json at all"),
                Arguments.of("{}"),
                Arguments.of("")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJsonData")
    @DisplayName("Invalid JSON & Missing Fields TEST")
    public void invalidJsonTests(String rawJson) {

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(rawJson)
                .when()
                .post(URL)
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad request"));
    }

    // ================= WITHOUT AUTH =================

    @Test
    @DisplayName("Transfer Without Auth")
    public void transferWithoutAuth() {

        double senderBefore = getCurrentBalance(1);
        double receiverBefore = getCurrentBalance(3);

        String body = """
                {
                   "senderAccountId": 1,
                   "receiverAccountId": 3,
                   "amount": 100
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(URL)
                .then()
                .statusCode(400)
                .body(equalTo("Unauthorized access to account"));

        double senderAfter = getCurrentBalance(1);
        double receiverAfter = getCurrentBalance(3);

        assertEquals(senderBefore, senderAfter);
        assertEquals(receiverBefore, receiverAfter);
    }
}
