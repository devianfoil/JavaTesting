package MiddleTest;

import CompraratorLogic.ModelComparator;
import Generators.RandomNumbers;
import Generators.InvalidJsonPayloads;
import models.TransferRequest;
import models.TransferResponse;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Skeleton.ProfileDataHelper;
import requests.Skeleton.Endpoint;
import requests.Skeleton.Requesters.CrudRequester;
import requests.Skeleton.Requesters.ValidatedCrudRequester;

import java.util.Map;
import java.util.stream.Stream;

import static Generators.TestErrorsAndStatusCodesConstants.*;
import static Generators.TransferTestsData.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponseSpecs.requestReturnsBadRequest;
import static specs.ResponseSpecs.requestReturnsOK;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class TransferTestsV2 extends BaseTest {


    // ================= REQUESTERS =================

    private ProfileDataHelper userProfile1;
    private ProfileDataHelper userProfile2;

    @BeforeEach
    void setUp() {
        CrudRequester profileCrud1 = new CrudRequester(
                authSpecUser1,
                Endpoint.PROFILE_REQUESTER,
                requestReturnsOK()
        );
        userProfile1 = new ProfileDataHelper(profileCrud1);

        CrudRequester profileCrud2 = new CrudRequester(
                authSpecUser2,
                Endpoint.PROFILE_REQUESTER,
                requestReturnsOK()
        );
        userProfile2 = new ProfileDataHelper(profileCrud2);


    }


    // ================= VALID TRANSFER =================

    static Stream<Double> validAmounts() {
        return Stream.of(VALID_MIN, RandomNumbers.randomDouble(VALID_MIN, VALID_MAX), VALID_MAX);
    }

    @ParameterizedTest
    @MethodSource("validAmounts")
    @DisplayName("Valid transfer should update balances and return correct response")
    void successfulTransfer(double amount) {

        int senderAccountId = userProfile1.getAccountIdWithMaxBalance();
        int receiverAccountId = userProfile1.getAccountIdWithMinBalanceExcluding(senderAccountId);

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile1.getBalance(receiverAccountId);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        ValidatedCrudRequester<TransferResponse> requester =
                new ValidatedCrudRequester<>(
                        authSpecUser1,
                        Endpoint.TRANSFER,
                        requestReturnsOK()
                );

        TransferResponse response = requester.post(request);

        ModelComparator.ComparisonResult comparison =
                ModelComparator.compareFields(
                        request,
                        response,
                        Map.of(
                                "amount", "amount",
                                "senderAccountId", "senderAccountId",
                                "receiverAccountId", "receiverAccountId"
                        )
                );

        assertThat(comparison.isSuccess())
                .as(comparison.toString())
                .isTrue();

        // ✅ STATE ASSERTIONS
        double senderAfter = userProfile1.getBalance(senderAccountId);
        double receiverAfter = userProfile1.getBalance(receiverAccountId);

        softly.assertThat(senderAfter)
                .as("Sender balance should decrease by transfer amount")
                .isEqualTo(senderBefore - amount);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should increase by transfer amount")
                .isEqualTo(receiverBefore + amount);
    }

    // ================= INVALID AMOUNTS =================

    static Stream<Double> invalidAmounts() {
        return Stream.of(
                INVALID_NEGATIVE,
                INVALID_ZERO,
                INVALID_OVER_LIMIT,
                INVALID_TOO_BIG
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    @DisplayName("Invalid transfer amount should not change balances")
    void invalidTransferAmount(double amount) {

        int senderAccountId = userProfile1.getAccountIdWithMaxBalance();
        int receiverAccountId = userProfile1.getAccountIdWithMinBalanceExcluding(senderAccountId);

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile1.getBalance(receiverAccountId);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.TRANSFER,
                requestReturnsBadRequest(
                        String.valueOf(BAD_REQUEST_STATUS),
                        TRANSFER_INVALID_MESSAGE
                )
        ).post(request);

        double senderAfter = userProfile1.getBalance(senderAccountId);
        double receiverAfter = userProfile1.getBalance(receiverAccountId);

        softly.assertThat(senderAfter)
                .as("Sender balance should not change on invalid amount")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change on invalid amount")
                .isEqualTo(receiverBefore);
    }

    // ================= WITHOUT AUTH =================

    @Test
    @DisplayName("Transfer without authorization should not change balances")
    void transferWithoutAuth() {

        int senderAccountId = userProfile1.getAccountIdWithMaxBalance();
        int receiverAccountId = userProfile1.getAccountIdWithMinBalanceExcluding(senderAccountId);

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile1.getBalance(receiverAccountId);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomNumbers.randomDouble(VALID_MIN,VALID_MAX))
                .build();

        new CrudRequester(
                unauthSpec(),
                Endpoint.TRANSFER,
                requestReturnsBadRequest(
                        String.valueOf(UNAUTHORIZED_STATUS),
                        EMPTY_BODY
                )
        ).post(request);

        double senderAfter = userProfile1.getBalance(senderAccountId);
        double receiverAfter = userProfile1.getBalance(receiverAccountId);

        softly.assertThat(senderAfter)
                .as("Sender balance should not change without auth")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change without auth")
                .isEqualTo(receiverBefore);
    }

    // ================= INVALID JSON =================

    @ParameterizedTest
    @MethodSource("Generators.InvalidJsonPayloads#invalidJsonPayloads")
    @DisplayName("Invalid JSON should not change balances")
    void invalidJson(String rawJson) {
        int senderAccountId = userProfile1.getAccountIdWithMaxBalance();
        int receiverAccountId = userProfile1.getAccountIdWithMinBalanceExcluding(senderAccountId);

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile1.getBalance(receiverAccountId);

        given()
                .spec(authSpecUser1)
                .contentType(ContentType.JSON)
                .body(rawJson)
                .when()
                .post(Endpoint.TRANSFER.getUrl())
                .then()
                .statusCode(greaterThanOrEqualTo(BAD_REQUEST_STATUS));

        double senderAfter = userProfile1.getBalance(senderAccountId);
        double receiverAfter = userProfile1.getBalance(receiverAccountId);

        softly.assertThat(senderAfter)
                .as("Sender balance should not change with invalid JSON")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change with invalid JSON")
                .isEqualTo(receiverBefore);
    }
}
