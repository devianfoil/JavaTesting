package MiddleTest;

import Generators.DepositBoundaryConstants;
import io.restassured.http.ContentType;
import models.DepositRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Skeleton.Endpoint;
import requests.Skeleton.ProfileDataHelper;
import requests.Skeleton.Requesters.CrudRequester;
import requests.Skeleton.Requesters.ValidateCrudRequester;

import java.util.stream.Stream;

import static Generators.DepositBoundaryConstants.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponseSpecs.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DepositTestsV2 extends BaseTest {

    private ProfileDataHelper userProfile;

    @BeforeEach
    void setUp() {
        CrudRequester profileCrud = new CrudRequester(
                authSpecUser1,
                Endpoint.PROFILE_REQUESTER,
                requestReturnsOK()
        );
        userProfile = new ProfileDataHelper(profileCrud);
    }

    // ================= VALID DEPOSIT =================

    static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(DepositBoundaryConstants.ZERO),
                Arguments.of(DepositBoundaryConstants.NEGATIVE),
                Arguments.of(DepositBoundaryConstants.TOO_BIG)


        );
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @DisplayName("Valid deposit should increase balance")
    void validDeposit(double amount) {

        int accountId = userProfile.getAccountId();
        double balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(amount)
                .build();

        new ValidateCrudRequester<>(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsOK()
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should increase by deposit amount")
                .isEqualTo(balanceBefore + amount);
    }

    // ================= INVALID AMOUNT =================

    static Stream<Arguments> invalidAmounts() {
        return Stream.of(
                Arguments.of(0.0),
                Arguments.of(-100.0),
                Arguments.of(5000.01),
                Arguments.of(99999.0)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    @DisplayName("Invalid deposit amount should not change balance")
    void invalidDepositAmount(double amount) {

        int accountId = userProfile.getAccountId();
        double balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(amount)
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(String.valueOf(BAD_REQUEST_STATUS), INVALID_AMOUNT_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should not change for invalid amount")
                .isEqualTo(balanceBefore);
    }

    // ================= INVALID ACCOUNT ID =================

    @Test
    @DisplayName("Invalid account id should not change balance")
    void invalidAccountId() {

        int validAccountId = userProfile.getAccountId();
        double balanceBefore = userProfile.getBalance(validAccountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(9999)
                .balance(100)
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(String.valueOf(BAD_REQUEST_STATUS),UNAUTHORIZED_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(validAccountId);

        assertThat(balanceAfter)
                .as("Balance should not change for invalid account id")
                .isEqualTo(balanceBefore);
    }

    // ================= WITHOUT AUTH =================

    @Test
    @DisplayName("Deposit without authorization should not change balance")
    void depositWithoutAuth() {

        int accountId = userProfile.getAccountId();
        double balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(100)
                .build();

        new CrudRequester(
                unauthSpec(),
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(String.valueOf(BAD_REQUEST_STATUS),UNAUTHORIZED_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should not change without authorization")
                .isEqualTo(balanceBefore);
    }

    // ================= INVALID JSON =================

    static Stream<Arguments> invalidJsonBodies() {
        return Stream.of(
                Arguments.of("{id:1, balance:100}"),
                Arguments.of("{\"id\":1 \"balance\":100}"),
                Arguments.of("not a json"),
                Arguments.of("{}"),
                Arguments.of("")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJsonBodies")
    @DisplayName("Invalid JSON should not change balance")
    void invalidJson(String rawJson) {

        int accountId = userProfile.getAccountId();
        double balanceBefore = userProfile.getBalance(accountId);

        given()
                .spec(authSpecUser1)
                .contentType(ContentType.JSON)
                .body(rawJson)
                .when()
                .post(Endpoint.DEPOSIT.getUrl())
                .then()
                .spec(requestReturnsInternalError());

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should not change for invalid JSON")
                .isEqualTo(balanceBefore);
    }
}
