package Iteration2;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.classfile.instruction.NewMultiArrayInstruction;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;
import static org.hamcrest.Matchers.equalTo;

public class ChangeUserNameTests {
    private static final String userToken = "Qm9nZGFuMjAwMjpCb2dkYW5pb18lMTIzNDVx";
    private static final String URL = "http://localhost:55002/api/v1/customer/profile";
    public static String currentUsername() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("http://localhost:55002/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .extract()
                .path("name");   // достаём поле нейм

    }
    @Test
    @DisplayName("Positive: Change username with valid params")
    public void changeUserNamePositive() {
        String userNameBeforeTest = currentUsername();

        String newName = "New Name";

        String body = """
                {
                    "name": "%s"
                }
                """.formatted(newName);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(body)
                .when()
                .put(URL)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo(newName));
        String userNameAfterTest = currentUsername();
        Assertions.assertNotEquals(userNameBeforeTest, userNameAfterTest);
        Assertions.assertEquals(newName, userNameAfterTest);

    }


    public static Stream<Arguments> dataForInvalidUserName() {
        return Stream.of(
                Arguments.of("NEWTEST"),
                Arguments.of(currentUsername()),
                Arguments.of("")
        );
    }


    @ParameterizedTest
    @MethodSource("dataForInvalidUserName")
    public void changeUserNameTests(String newUserName) {
        String userNameBeforeTest = currentUsername();

        String requestBody = """
                {
                
                     "name": "%s"
                
                }
                """.formatted(newUserName);
        given()


                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestBody)
                .when()
                .put(URL)
                .then()
                .assertThat()
                .statusCode(500)
                .body("message", equalTo("Profile name not changed due to invalid user name."));
                 String userNameAfterTest = currentUsername();
                 Assertions.assertEquals(userNameBeforeTest, userNameAfterTest);

    }

    @Test
    @DisplayName("Negative: Change username without authorization")
    public void changeUserNameNoAuth() {
        String userNameBeforeTest = currentUsername();


        String body = """
                {
                    "name": "New Name"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(URL)
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo("Unauthorized access to account"));
        String userNameAfterTest =currentUsername();
        Assertions.assertEquals(userNameBeforeTest, userNameAfterTest);
    }


}
