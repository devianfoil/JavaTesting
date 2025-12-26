package MiddleTest;

import models.DepositRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.DepositRequester;
import requests.ProfileDataRequester;

import java.util.stream.Stream;

import static requests.DepositRequester.*;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponсeSpecs.*;

public class DepositTestsV2 extends BaseTest {

    private ProfileDataRequester userProfile;
    private DepositRequester depositRequester;

    @BeforeEach
    void setUp() {
        userProfile = user1Profile;
        depositRequester = new DepositRequester(authSpecUser1, requestReturnsOK());
    }

    // ================= VALID DEPOSIT =================

    static Stream<Arguments> dataForValidDeposit() {
        return Stream.of(
                Arguments.of(100.00),
                Arguments.of(4999.99),
                Arguments.of(0.01)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForValidDeposit")
    @DisplayName("Deposit with valid values should increase balance")
    void successfulDeposit(double depositAmount) {

        int accountId = userProfile.getAccountID();
        double balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(depositAmount)
                .build();

        depositRequester.post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        softly.assertThat(balanceAfter)
                .as("Balance should increase by deposit amount")
                .isEqualTo(balanceBefore + depositAmount);
    }

    // ================= INVALID SUMS =================

    static Stream<Arguments> dataForInvalidSums() {
        return Stream.of(
                Arguments.of(0.0),
                Arguments.of(-100.0),
                Arguments.of(5000.01),
                Arguments.of(99999.0)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidSums")
    @DisplayName("Invalid deposit amounts should not change balance")
    void invalidDepositAmount(double amount) {

        int accountId = userProfile.getAccountID();
        double balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(amount)
                .build();

        new DepositRequester(
                authSpecUser1,
                requestReturnsBadRequest(String.valueOf(BAD_REQUEST_STATUS), INVALID_AMOUNT_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        softly.assertThat(balanceAfter)
                .as("Balance should not change for invalid deposit amount")
                .isEqualTo(balanceBefore);
    }

    // ================= INVALID ACCOUNT ID =================

    static Stream<Arguments> invalidAccountIds() {
        return Stream.of(
                Arguments.of(909),
                Arguments.of(3)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAccountIds")
    @DisplayName("Invalid account id should not change balance")
    void invalidAccountId(int invalidAccountId) {

        int validAccountId = userProfile.getAccountID();
        double balanceBefore = userProfile.getBalance(validAccountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(invalidAccountId)
                .balance(100)
                .build();

        new DepositRequester(
                authSpecUser1,
                requestReturnsBadRequest(String.valueOf(FORBIDDEN_STATUS), UNAUTHORIZED_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(validAccountId);

        softly.assertThat(balanceAfter)
                .as("Balance should not change for invalid account id")
                .isEqualTo(balanceBefore);
    }

    // ================= WITHOUT AUTH =================

    @Test
    @DisplayName("Deposit without authorization should not change balance")
    void depositWithoutAuth() {

        int accountId = userProfile.getAccountID();
        double balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(100)
                .build();

        new DepositRequester(
                unauthSpec(),
                requestReturnsBadRequest(String.valueOf(BAD_REQUEST_STATUS),UNAUTHORIZED_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        softly.assertThat(balanceAfter)
                .as("Balance should not change without authorization")
                .isEqualTo(balanceBefore);
    }

    // ================= INVALID JSON =================

    static Stream<Arguments> invalidJsonBodies() {
        return Stream.of(
                Arguments.of("{id:1, balance:100}"),
                Arguments.of("{\"id\":1 \"balance\":100}"),
                Arguments.of("not a json at all"),
                Arguments.of("{}"),
                Arguments.of("")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJsonBodies")
    @DisplayName("Invalid JSON should not change balance")
    void invalidJsonBody(String rawJson) {

        int accountId = userProfile.getAccountID();
        double balanceBefore = userProfile.getBalance(accountId);

        new DepositRequester(
                authSpecUser1,
                requestReturnsBadRequest(String.valueOf(INTERNAL_ERROR_STATUS),INTERNAL_ERROR_MESSAGE)
        ).postRaw(rawJson);

        double balanceAfter = userProfile.getBalance(accountId);

        softly.assertThat(balanceAfter)
                .as("Balance should not change for invalid JSON")
                .isEqualTo(balanceBefore);
    }
}
