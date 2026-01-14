package MiddleTest;

import models.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.ProfileDataRequester;

import java.util.stream.Stream;

import static requests.TransferRequester.*;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponсeSpecs.requestReturnsBadRequest;
import static specs.ResponсeSpecs.requestReturnsOK;

public class TransferTestsV2 extends BaseTest {

    private ProfileDataRequester user1;
    private ProfileDataRequester user2;
    private TransferRequester transferRequester;

    @BeforeEach
    void setUp() {
        user1 = user1Profile;
        user2 = user2Profile;
        transferRequester = new TransferRequester(authSpecUser1, requestReturnsOK());
    }

    // ================= VALID TRANSFER =================

    static Stream<Arguments> dataForValidTransfer() {
        return Stream.of(
                Arguments.of(50.00),
                Arguments.of(0.01),
                Arguments.of(4999.99)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForValidTransfer")
    @DisplayName("Transfer with valid values should update balances")
    void successfulTransfer(double amount) {

        double senderBefore = user1.getBalance(user1.getAccountID());
        double receiverBefore = user2.getBalance(user2.getAccountID());

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(user1.getAccountID())
                .receiverAccountId(user2.getAccountID())
                .amount(amount)
                .build();

        transferRequester.post(request);

        double senderAfter = user1.getBalance(user1.getAccountID());
        double receiverAfter = user2.getBalance(user2.getAccountID());

        softly.assertThat(senderAfter)
                .as("Sender balance should decrease by transfer amount")
                .isEqualTo(senderBefore - amount);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should increase by transfer amount")
                .isEqualTo(receiverBefore + amount);
    }

    // ================= INVALID AMOUNTS =================

    static Stream<Arguments> dataForInvalidTransfer() {
        return Stream.of(
                Arguments.of(-50.0),
                Arguments.of(0.0),
                Arguments.of(10000.0),
                Arguments.of(5000.01)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidTransfer")
    @DisplayName("Transfer with invalid amount should not change balances")
    void invalidTransferAmount(double amount) {

        double senderBefore = user1.getBalance(user1.getAccountID());
        double receiverBefore = user2.getBalance(user2.getAccountID());

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(user1.getAccountID())
                .receiverAccountId(user2.getAccountID())
                .amount(amount)
                .build();

        new TransferRequester(
                authSpecUser1,
                requestReturnsBadRequest(String.valueOf(TransferRequester.BAD_REQUEST_STATUS), INVALID_AMOUNT_MESSAGE)
        ).post(request);

        double senderAfter = user1.getBalance(user1.getAccountID());
        double receiverAfter = user2.getBalance(user2.getAccountID());

        softly.assertThat(senderAfter)
                .as("Sender balance should not change on invalid transfer")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change on invalid transfer")
                .isEqualTo(receiverBefore);
    }

    // ================= INVALID ACCOUNT IDS =================

    static Stream<Arguments> dataForInvalidAccountIds() {
        return Stream.of(
                Arguments.of(99, 2),
                Arguments.of(1, 999),
                Arguments.of(0, 2),
                Arguments.of(1, 0),
                Arguments.of(1, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidAccountIds")
    @DisplayName("Transfer with invalid account ids should not change balances")
    void invalidAccountIds(int senderId, int receiverId) {

        double senderBefore = user1.getBalance(user1.getAccountID());
        double receiverBefore = user2.getBalance(user2.getAccountID());

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(500)
                .build();

        new TransferRequester(
                authSpecUser1,
                requestReturnsBadRequest(String.valueOf(TransferRequester.BAD_REQUEST_STATUS), UNAUTHORIZED_MESSAGE)
        ).post(request);

        double senderAfter = user1.getBalance(user1.getAccountID());
        double receiverAfter = user2.getBalance(user2.getAccountID());

        softly.assertThat(senderAfter)
                .as("Sender balance should not change for invalid IDs")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change for invalid IDs")
                .isEqualTo(receiverBefore);
    }

    // ================= WITHOUT AUTH =================

    @Test
    @DisplayName("Transfer without authorization should fail")
    void transferWithoutAuth() {

        double senderBefore = user1.getBalance(user1.getAccountID());
        double receiverBefore = user2.getBalance(user2.getAccountID());

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(user1.getAccountID())
                .receiverAccountId(user2.getAccountID())
                .amount(500)
                .build();

        new TransferRequester(
                unauthSpec(),
                requestReturnsBadRequest(String.valueOf(TransferRequester.FORBIDDEN_STATUS), UNAUTHORIZED_MESSAGE)
        ).post(request);

        double senderAfter = user1.getBalance(user1.getAccountID());
        double receiverAfter = user2.getBalance(user2.getAccountID());

        softly.assertThat(senderAfter)
                .as("Sender balance should not change without auth")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change without auth")
                .isEqualTo(receiverBefore);
    }

    // ================= INVALID JSON =================

    static Stream<Arguments> invalidJsonData() {
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
    @DisplayName("Invalid JSON should not change balances")
    void invalidJsonTest(String rawJson) {

        double senderBefore = user1.getBalance(user1.getAccountID());
        double receiverBefore = user2.getBalance(user2.getAccountID());

        new TransferRequester(
                unauthSpec(),
                requestReturnsBadRequest(String.valueOf(TransferRequester.INTERNAL_ERROR_STATUS), INTERNAL_ERROR_MESSAGE)
        ).postRaw(rawJson);

        double senderAfter = user1.getBalance(user1.getAccountID());
        double receiverAfter = user2.getBalance(user2.getAccountID());

        softly.assertThat(senderAfter)
                .as("Sender balance should not change on invalid JSON")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change on invalid JSON")
                .isEqualTo(receiverBefore);
    }
}
