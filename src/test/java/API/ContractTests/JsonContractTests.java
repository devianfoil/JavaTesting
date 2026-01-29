package API.ContractTests;

import API.MiddleTest.BaseTest;
import Generators.InvalidJsonPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Skeleton.Endpoint;
import requests.Skeleton.Requesters.CrudRequester;

import static Generators.TestErrorsAndStatusCodesConstants.*;
import static specs.RequestSpecs.unauthSpec;
import static specs.ResponseSpecs.requestReturnsBadRequest;

public class JsonContractTests extends BaseTest {

    @ParameterizedTest
    @MethodSource("Generators.InvalidJsonPayloads#invalidJsonPayloads")
    @DisplayName("Invalid JSON should return bad request for transfer endpoint")
    void transferInvalidJson(String rawJson) {
        new CrudRequester(
                authSpecUser1,
                Endpoint.TRANSFER,
                requestReturnsBadRequest(
                        String.valueOf(BAD_REQUEST_STATUS),
                        TRANSFER_INVALID_MESSAGE
                )
        ).postRaw(rawJson);
    }

    @ParameterizedTest
    @MethodSource("Generators.InvalidJsonPayloads#invalidJsonPayloads")
    @DisplayName("Invalid JSON should return bad request for change username endpoint")
    void changeUsernameInvalidJson(String rawJson) {
        new CrudRequester(
                authSpecUser1,
                Endpoint.CHANGEUSERNAME,
                requestReturnsBadRequest(
                        String.valueOf(UNAUTHORIZED_STATUS),
                        EMPTY_BODY
                )
        ).putRaw(rawJson);
    }

    @ParameterizedTest
    @MethodSource("Generators.InvalidJsonPayloads#invalidJsonPayloads")
    @DisplayName("Invalid JSON should return bad request for deposit endpoint")
    void depositInvalidJson(String rawJson) {
        new CrudRequester(
                authSpecUser1,
                Endpoint.DEPOSIT,
                requestReturnsBadRequest(
                        String.valueOf(INTERNAL_ERROR_STATUS),
                        INVALID_AMOUNT_MESSAGE
                )
        ).postRaw(rawJson);
    }






}
