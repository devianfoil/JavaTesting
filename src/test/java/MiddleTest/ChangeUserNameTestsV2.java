package MiddleTest;

import CompraratorLogic.ModelAssertions;
import Generators.RandomModelGenerator;
import Generators.InvalidJsonPayloads;
import io.restassured.http.ContentType;
import models.UpdateProfileRequest;
import models.UpdateProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Skeleton.Endpoint;
import requests.Skeleton.ProfileDataHelper;
import requests.Skeleton.Requesters.CrudRequester;
import requests.Skeleton.Requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

import static Generators.TestErrorsAndStatusCodesConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponseSpecs.requestReturnsBadRequest;
import static specs.ResponseSpecs.requestReturnsOK;

public class ChangeUserNameTestsV2 extends BaseTest {

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

    @Test
    @DisplayName("Positive: Change username with valid params")
    void changeUserNamePositive() {

        // ARRANGE
        String nameBefore = userProfile.getName();

        UpdateProfileRequest request =
                RandomModelGenerator.generate(UpdateProfileRequest.class);

        ValidatedCrudRequester<UpdateProfileResponse> crud =
                new ValidatedCrudRequester<>(
                        authSpecUser1,
                        Endpoint.CHANGEUSERNAME,
                        requestReturnsOK()
                );

        UpdateProfileResponse response = crud.put(request);

        String nameAfter = userProfile.getName();

        // ASSERT — state
        softly.assertThat(nameAfter)
                .as("Username should be updated")
                .isEqualTo(request.getName());

        softly.assertThat(nameAfter)
                .as("Username should differ from previous value")
                .isNotEqualTo(nameBefore);

        // ASSERT — contract (MODEL COMPARISON)
        ModelAssertions.assertThatModels(request, response)
                .match();
    }


    static Stream<Arguments> dataForInvalidUserName() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("NEWTEST"),
                Arguments.of((String) null)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForInvalidUserName")
    @DisplayName("Negative: Change username with invalid values")
    void changeUserNameInvalid(String newUserName) {
        String nameBefore = userProfile.getName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(newUserName)
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.CHANGEUSERNAME,
                requestReturnsOK()
        ).put(request);

        String nameAfter = userProfile.getName();

        softly.assertThat(nameAfter)
                .as("Username should match the requested value")
                .isEqualTo(newUserName);
    }

    @Test
    @DisplayName("Negative: Change username without authorization")
    void changeUserNameNoAuth() {
        String nameBefore = userProfile.getName();

        UpdateProfileRequest request =
                RandomModelGenerator.generate(UpdateProfileRequest.class);

        new CrudRequester(
                unauthSpec(),
                Endpoint.CHANGEUSERNAME,
                requestReturnsBadRequest(
                        String.valueOf(UNAUTHORIZED_STATUS),
                        EMPTY_BODY
                )
        ).put(request);

        String nameAfter = userProfile.getName();

        softly.assertThat(nameAfter)
                .as("Username should not be changed without authorization")
                .isEqualTo(nameBefore);
    }

    @Test
    @DisplayName("Negative: Change username to the same value")
    void changeUserNameToSameValue() {
        String currentName =userProfile.getName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(currentName)
                .build();

        new CrudRequester(
                authSpecUser1,
                Endpoint.CHANGEUSERNAME,
                requestReturnsOK()
        ).put(request);

        String nameAfter = userProfile.getName();

        softly.assertThat(nameAfter)
                .as("Username should remain the same if new value equals old value")
                .isEqualTo(currentName);
    }

    // ================= INVALID JSON =================

    @ParameterizedTest
    @MethodSource("Generators.InvalidJsonPayloads#invalidJsonPayloads")
    @DisplayName("Invalid JSON should not change username")
    void invalidJson(String rawJson) {
        String nameBefore = userProfile.getName();

        given()
                .spec(authSpecUser1)
                .contentType(ContentType.JSON)
                .body(rawJson)
                .when()
                .put(Endpoint.CHANGEUSERNAME.getUrl())
                .then()
                .statusCode(greaterThanOrEqualTo(BAD_REQUEST_STATUS));

        String nameAfter = userProfile.getName();

        softly.assertThat(nameAfter)
                .as("Username should not change with invalid JSON")
                .isEqualTo(nameBefore);
    }
}
