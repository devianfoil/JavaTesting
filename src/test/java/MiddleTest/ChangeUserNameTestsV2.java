package MiddleTest;

import Generator.RandomDataGenerator;
import models.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.ChangeUserNameRequester;
import requests.ProfileDataRequester;

import java.util.stream.Stream;

import static requests.ChangeUserNameRequester.*;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponсeSpecs.*;

public class ChangeUserNameTestsV2 extends BaseTest {

    private ProfileDataRequester profileDataRequester;
    private ChangeUserNameRequester changeUserNameRequester;

    @BeforeEach
    void setUp() {
        profileDataRequester = new ProfileDataRequester(authSpecUser1);

        changeUserNameRequester = new ChangeUserNameRequester(
                authSpecUser1,
                requestReturnsOK()
        );
    }

    @Test
    @DisplayName("Positive: Change username with valid params")
    void changeUserNamePositive() {
        // ARRANGE
        String nameBefore = profileDataRequester.getAccountName();
        String newName = RandomDataGenerator.randomUsername();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(newName)
                .build();

        // ACT
        changeUserNameRequester.put(request);

        String nameAfter = profileDataRequester.getAccountName();

        // ASSERT
        softly.assertThat(nameAfter)
                .as("Username should be updated")
                .isEqualTo(newName);

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
                requestReturnsBadRequest(
                        String.valueOf(INVALID_NAME_STATUS),
                        ChangeUserNameRequester.INVALID_NAME_MESSAGE
                )
        ).put(request);

        String nameAfter = profileDataRequester.getAccountName();

        softly.assertThat(nameAfter)
                .as("Username should not be changed for invalid input")
                .isEqualTo(nameBefore);
    }

    @Test
    @DisplayName("Negative: Change username without authorization")
    void changeUserNameNoAuth() {
        String nameBefore = profileDataRequester.getAccountName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(RandomDataGenerator.randomUsername())
                .build();

        new ChangeUserNameRequester(
                unauthSpec(),
                requestReturnsBadRequest(
                        String.valueOf(UNAUTHORIZED_STATUS),
                        UNAUTHORIZED_MESSAGE
                )
        ).put(request);

        String nameAfter = profileDataRequester.getAccountName();

        softly.assertThat(nameAfter)
                .as("Username should not be changed without authorization")
                .isEqualTo(nameBefore);
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

        softly.assertThat(nameAfter)
                .as("Username should remain the same if new value equals old value")
                .isEqualTo(currentName);
    }
}
