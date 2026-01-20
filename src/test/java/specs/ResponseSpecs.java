package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs() {}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, String errorValue) {
        int statusCode;
        try {
            statusCode = Integer.parseInt(errorKey);
        } catch (NumberFormatException e) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        }

        return defaultResponseBuilder()
                .expectStatusCode(statusCode)
                .expectBody(Matchers.containsString(errorValue))
                .build();
    }
}