package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.DepositRequest;

import static io.restassured.RestAssured.*;

public class DepositRequester extends PostRequest<DepositRequest> {

    public DepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(DepositRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
// метод нужен для битого JSOn - так как будто проще это сделать чем билдить параметры отдельно битыми
    public ValidatableResponse postRaw(String rawBody) {
        given()
                .spec(requestSpecification)
                .body(rawBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .spec(responseSpecification);
        return null;
    }


}
