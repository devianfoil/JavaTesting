package MiddleTest;

import models.UpdateProfileRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.ChangeUserNameRequester;
import requests.ProfileDataRequester;

import java.util.stream.Stream;

import static specs.RequestSpecs.unauthSpec;
import static specs.ResponсeSpecs.requestReturnsError;
import static specs.ResponсeSpecs.requestReturnsOK;

public class ChangeUserNameTestsV2 extends BaseTest {

    private ProfileDataRequester profileDataRequester;
    private ChangeUserNameRequester changeUserNameRequester;

    @BeforeEach
    void setUp() {
        profileDataRequester = user1Profile;
        changeUserNameRequester = new ChangeUserNameRequester(
                authSpecUser1,
                requestReturnsOK()
        );
    }

    @Test
    @DisplayName("Positive: Change username with valid params")
    void changeUserNamePositive() {
        String nameBefore = user1Profile.getAccountName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("New NAME")
                .build();

        changeUserNameRequester.put(request);

        String nameAfter = user1Profile.getAccountName();

        softly.assertThat(nameAfter)
                .as("Username should be updated")
                .isEqualTo("New NAME");

        softly.assertThat(nameAfter)
                .as("Username should differ from previous value")
                .isNotEqualTo(nameBefore);
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
        String nameBefore = profileDataRequester.getAccountName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(newUserName)
                .build();

        new ChangeUserNameRequester(
                authSpecUser1,
                requestReturnsError(
                        500,
                        "Profile name not changed due to invalid user name."
                )
        ).put(request);

        String nameAfter = profileDataRequester.getAccountName();

        Assertions.assertEquals(nameBefore, nameAfter);
    }

    @Test
    @DisplayName("Negative: Change username without authorization")
    void changeUserNameNoAuth() {
        String nameBefore = profileDataRequester.getAccountName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Another NAME")
                .build();

        new ChangeUserNameRequester(
                unauthSpec(),
                requestReturnsError(400, "Unauthorized access to account")
        ).put(request);

        String nameAfter = profileDataRequester.getAccountName();

        Assertions.assertEquals(nameBefore, nameAfter);
    }

    @Test
    @DisplayName("Negative: Change username to the same value")
    void changeUserNameToSameValue() {
        String currentName = profileDataRequester.getAccountName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(currentName)
                .build();

        changeUserNameRequester.put(request);

        String nameAfter = profileDataRequester.getAccountName();

        Assertions.assertEquals(currentName, nameAfter);
    }
}
