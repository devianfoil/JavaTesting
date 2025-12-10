package Iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TransferTests {
    private static final String userToken = "Qm9nZGFuMjAwMjpCb2dkYW5pb18lMTIzNDVx";
    private static final String VALID_TRANSFER_MESSAGE = "Transfer successful";
    private static final String INVALID_OR_INSUFFICIENT_FUNDS_MESSAGE = "insufficient funds or invalid accounts";
    private static final String INVALID_IDS_MESSAGE = "Unauthorized access to account";

    private static final String URL = "/api/v1/accounts/transfer";


    public static Stream<Arguments> dataForValidTransfer() {
        return Stream.of(
                Arguments.of(50.00),
                Arguments.of(0.01),
                Arguments.of(4999.99)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForValidTransfer")
    @DisplayName(" Positive Transfer test with different sums")
    public void validTransferTest(double amount) {

        String requestBody = """
                {
                   "senderAccountId": 1,
                   "receiverAccountId": 3,
                   "amount":%.2f
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
                .assertThat()
                .body("receiverAccountId", equalTo(3))
                .body("amount", equalTo(amount))
                .body("senderAccountId", equalTo(1))
                .body("message", equalTo(VALID_TRANSFER_MESSAGE));
    }


    public static Stream<Arguments> dataForInvalidTransfer() {
        return Stream.of(
                Arguments.of(-50),// negative sum
                Arguments.of(0),// zero case
                Arguments.of(10000),//sum that is bigger an account
                Arguments.of(5000.01)// boundary sum

        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidTransfer")
    @DisplayName("Negative transfer test with invalidsums")
    public void InvalidTransferTest(double amount) {

        String requestBody = """
                {
                   "senderAccountId": 1,
                   "receiverAccountId":4,
                   "amount":%.2f
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
                .assertThat()
                .statusCode(400)
                .body("Invalid transfer", equalTo(INVALID_OR_INSUFFICIENT_FUNDS_MESSAGE));
    }

    public static Stream<Arguments> dataForInvalidParamsTesT() {
        return Stream.of(
                Arguments.of(99, 2),//invalid sender id
                Arguments.of(1, 999),// invalid receiver id
                Arguments.of(0, 2),// zero case 1
                Arguments.of(1, 0),// zero case 2
                Arguments.of(1, 1)// transfer to the same account - тут похоже на баг самого бека так как нельзя же
                //перевести на 1 и тот же аккаунт


        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidParamsTesT")
    @DisplayName("Negative transfer tests with invalid account account id's")
    public void InvalidTransferTestWithWrongParams(int senderID, int receiverID) {
        String requestBody = """
                {
                   "senderAccountId": %d,
                   "receiverAccountId":%d,
                   "amount":50
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
                .assertThat()
                .statusCode(403)
                .body(equalTo(INVALID_IDS_MESSAGE));
    }

    public static Stream<Arguments> invalidJsonData() {
        return Stream.of(

                // missing fields
                Arguments.of("""
                        {
                          "receiverAccountId": 3,
                          "amount": 50
                        }
                        """),
                Arguments.of("""
                        {
                          "senderAccountId": 1,
                          "amount": 50
                        }
                        """),
                Arguments.of("""
                        {
                          "senderAccountId": 1,
                          "receiverAccountId": 3
                        }
                        """),

                // invalid types
                Arguments.of("""
                        {
                          "senderAccountId": "abc",
                          "receiverAccountId": 3,
                          "amount": 50
                        }
                        """),
                Arguments.of("""
                        {
                          "senderAccountId": 1,
                          "receiverAccountId": "wrong",
                          "amount": 50
                        }
                        """),
                Arguments.of("""
                        {
                          "senderAccountId": 1,
                          "receiverAccountId": 3,
                          "amount": "text"
                        }
                        """),

                // invalid JSON syntax
                Arguments.of("{senderAccountId:1 receiverAccountId:3 amount:50}"),
                Arguments.of("{ \"senderAccountId\": 1 \"amount\": 50 }"),
                Arguments.of("not a json at all"),

                // empty JSON
                Arguments.of("{}"),

                // empty body
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
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("Bad request"));
    }

    @Test
    @DisplayName("Transfer Without Auth -> 400 Unauthorized access to account")
    public void transferWithoutAuth() {

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
                .assertThat()
                .statusCode(400)
                .body(equalTo("Unauthorized access to account"));
    }


}


