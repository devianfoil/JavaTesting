package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferRequest;

import static io.restassured.RestAssured.given;

public class TransferRequester extends PostRequest<TransferRequest> {
    // ===== HTTP STATUSES =====
    public static final int OK_STATUS = 200;
    public static final int BAD_REQUEST_STATUS = 400;
    public static final int FORBIDDEN_STATUS = 403;
    public static final int INTERNAL_ERROR_STATUS = 500;



    // ===== ERROR MESSAGES =====
    public static final String INVALID_AMOUNT_MESSAGE =
            "insufficient funds or invalid accounts";

    public static final String UNAUTHORIZED_MESSAGE =
            "Unauthorized access to account";

    public static final String INTERNAL_ERROR_MESSAGE =
            "Internal Server Error";

    public TransferRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(TransferRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
    public ValidatableResponse postRaw(String rawBody) {
        given()
                .spec(requestSpecification)
                .body(rawBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .spec(responseSpecification);
        return null;
    }
}
