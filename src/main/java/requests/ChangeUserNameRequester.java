package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.DepositRequest;
import models.UpdateProfileRequest;

import static io.restassured.RestAssured.given;

public class ChangeUserNameRequester extends PutRequest<UpdateProfileRequest> {
    public ChangeUserNameRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    // ===== API constants =====

    public static final int OK_STATUS = 200;

    public static final int UNAUTHORIZED_STATUS = 400;
    public static final String UNAUTHORIZED_MESSAGE =
            "Unauthorized access to account";

    public static final int INVALID_NAME_STATUS = 500;
    public static final String INVALID_NAME_MESSAGE =
            "Profile name not changed due to invalid user name.";



    @Override
    public ValidatableResponse put(UpdateProfileRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);

    }
}