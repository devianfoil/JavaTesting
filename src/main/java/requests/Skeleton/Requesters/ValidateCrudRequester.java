package requests.Skeleton.Requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.Skeleton.Endpoint;
import requests.Skeleton.HttpRequest;
import requests.Skeleton.Interface.CrudMethods;


public class ValidateCrudRequester<T extends BaseModel> extends HttpRequest implements CrudMethods {

    private final CrudRequester requester;

    public ValidateCrudRequester(
            RequestSpecification requestSpecification,
            Endpoint endpoint,
            ResponseSpecification responseSpecification
    ) {
        super(requestSpecification, endpoint, responseSpecification);
        this.requester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) requester
                .post(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T get() {
        return (T) requester
                .get()
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T put(BaseModel model) {
        return (T) requester
                .put(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public ValidatableResponse delete(long id) {
        return requester.delete(id);
    }
}

