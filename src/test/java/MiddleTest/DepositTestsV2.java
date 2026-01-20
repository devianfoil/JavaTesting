package MiddleTest;

import Generators.TestDataBoundariesConstants;
import Generators.RandomNumbers;
import Generators.InvalidJsonPayloads;
import io.restassured.http.ContentType;
import models.DepositRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Skeleton.Endpoint;
import requests.Skeleton.ProfileDataHelper;
import requests.Skeleton.Requesters.CrudRequester;
import requests.Skeleton.Requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

import static Generators.TestDataBoundariesConstants.*;
import static Generators.TestErrorsAndStatusCodesConstants.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponseSpecs.*;

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
                Arguments.of(RandomNumbers.randomDouble(MIN_DEPOSIT_VALUES, MAX_DEPOSIT_VALUES)),
                Arguments.of(TestDataBoundariesConstants.MIN_DEPOSIT_VALUES),
                Arguments.of(TestDataBoundariesConstants.MAX_DEPOSIT_VALUES)


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

        new ValidatedCrudRequester<>(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsOK()
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should increase by deposit amount")
                .isCloseTo(balanceBefore + amount, org.assertj.core.data.Offset.offset(0.01));
    }

    // ================= INVALID AMOUNT =================

    static Stream<Arguments> invalidAmounts() {
        return Stream.of(
                Arguments.of(ZERO),
                Arguments.of(RandomNumbers.moneyNegative(MIN_DEPOSIT_VALUES, MAX_DEPOSIT_VALUES))
                ,Arguments.of(TOO_BIG)

        );
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    @DisplayName("Invalid deposit amount should not change balance")
    void invalidDepositAmount(double amount) {

        int accountId = userProfile.getAccountId();
        float balanceBefore = userProfile.getBalance(accountId);

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .balance(amount)
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(String.valueOf(INTERNAL_ERROR_STATUS), INVALID_AMOUNT_MESSAGE)
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
                .accountId(INVALID_ACCOUNT_ID)
                .balance(RandomNumbers.randomDouble(MIN_DEPOSIT_VALUES, MAX_DEPOSIT_VALUES))
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(String.valueOf(FORBIDDEN_STATUS), UNAUTHORIZED_MESSAGE)
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
                .balance(RandomNumbers.randomDouble(MIN_DEPOSIT_VALUES, MAX_DEPOSIT_VALUES))
                .build();

        new CrudRequester(
                unauthSpec(),
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(String.valueOf(BAD_REQUEST_STATUS), UNAUTHORIZED_MESSAGE)
        ).post(request);

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should not change without authorization")
                .isEqualTo(balanceBefore);
    }

    // ================= INVALID JSON =================

    @ParameterizedTest
    @MethodSource("Generators.InvalidJsonPayloads#invalidJsonPayloads")
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
                .statusCode(greaterThanOrEqualTo(BAD_REQUEST_STATUS));

        double balanceAfter = userProfile.getBalance(accountId);

        assertThat(balanceAfter)
                .as("Balance should not change with invalid JSON")
                .isEqualTo(balanceBefore);
    }
}
