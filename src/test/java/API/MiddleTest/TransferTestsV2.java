package API.MiddleTest;

import CompraratorLogic.ModelAssertions;
import Generators.RandomNumbers;
import models.DepositRequest;
import models.TransferRequest;
import models.TransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Skeleton.ProfileDataHelper;
import requests.Skeleton.Endpoint;
import requests.Skeleton.Requesters.CrudRequester;
import requests.Skeleton.Requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

import static Generators.TestErrorsAndStatusCodesConstants.*;
import static Generators.TransferTestsData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponseSpecs.requestReturnsBadRequest;
import static specs.ResponseSpecs.requestReturnsOK;

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

        // Fund sender account for all tests
        int senderAccountId = userProfile1.getAccountIdWithMaxBalance();
        double fundAmount = RandomNumbers.randomDouble(VALID_MAX, VALID_MAX);
        DepositRequest fundRequest = DepositRequest.builder()
                .accountId(senderAccountId)
                .balance(fundAmount)
                .build();
        
        new CrudRequester(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsOK()
        ).post(fundRequest);
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
        int receiverAccountId = userProfile2.getAccountId();

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile2.getBalance(receiverAccountId);

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

        ModelAssertions.assertThatModels(request, response)
                .match();

        double senderAfter = userProfile1.getBalance(senderAccountId);
        double receiverAfter = userProfile2.getBalance(receiverAccountId);

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
        int receiverAccountId = userProfile2.getAccountId();

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile2.getBalance(receiverAccountId);

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
        double receiverAfter = userProfile2.getBalance(receiverAccountId);

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
        int receiverAccountId = userProfile2.getAccountId();

        double senderBefore = userProfile1.getBalance(senderAccountId);
        double receiverBefore = userProfile2.getBalance(receiverAccountId);

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
        double receiverAfter = userProfile2.getBalance(receiverAccountId);

        softly.assertThat(senderAfter)
                .as("Sender balance should not change without auth")
                .isEqualTo(senderBefore);

        softly.assertThat(receiverAfter)
                .as("Receiver balance should not change without auth")
                .isEqualTo(receiverBefore);
    }
}
