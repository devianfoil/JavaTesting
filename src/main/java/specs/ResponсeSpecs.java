package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.equalTo;

public class ResponсeSpecs {
    private ResponсeSpecs() {}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }



    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }


    public static ResponseSpecification requestReturnsError(int statusCode, Object expectedBody) {
        ResponseSpecBuilder builder =
                defaultResponseBuilder().expectStatusCode(statusCode);

        if (expectedBody instanceof String) {
            builder.expectBody(equalTo(expectedBody));
        }

        return builder.build();
    }
    }
