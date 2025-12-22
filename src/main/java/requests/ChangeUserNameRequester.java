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