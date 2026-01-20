package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginRequest;
import requests.Skeleton.Endpoint;
import requests.Skeleton.Requesters.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {
    private static Map<String, String> authHeaders = new HashMap<>();

    private RequestSpecs(){}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters( List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Configs.ServerConfig.getProperty("server") + Configs.ServerConfig.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }


    public static RequestSpecification authUser(String username, String password) {
        String userAuthHeader;

        if (!authHeaders.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            authHeaders.put(username, userAuthHeader);
        } else {
            userAuthHeader = authHeaders.get(username);
        }

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }
    }

