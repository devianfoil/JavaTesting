package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.LoginRequest;


import static io.restassured.RestAssured.given;

public class LoginRequester extends PostRequest<LoginRequest> {

    public LoginRequester(RequestSpecification requestSpecification,
                          ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(LoginRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/auth/login")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
//Пришлось имплементировать -в принципе тоже может быть полезным для теста невалидного JSON в логине
    @Override
    public ValidatableResponse postRaw(String rawBody) {
         given()
                .spec(requestSpecification)
                .body(rawBody)
                .post("/api/v1/auth/login")
                .then()
                .assertThat()
                .spec(responseSpecification);
        return null;

    }

}
