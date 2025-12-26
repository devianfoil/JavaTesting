package requests;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class GetRequest {
    protected RequestSpecification requestSpecification;

    public GetRequest(RequestSpecification requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public   <T> T get(String UrlPath, String jsonPath) {
        return given()
                .spec(requestSpecification)
                .when()
                .get(UrlPath)
                .then()
                .statusCode(200)
                .extract()
                .path(jsonPath);
    }


}

