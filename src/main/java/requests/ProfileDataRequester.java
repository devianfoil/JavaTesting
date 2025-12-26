package requests;

import io.restassured.specification.RequestSpecification;

public class ProfileDataRequester extends GetRequest {
    public ProfileDataRequester(RequestSpecification requestSpecification) {
        super(requestSpecification);

    }

    public double getBalance(int accountId) {
        return get(
                "/api/v1/customer/profile",
                "accounts.find { it.id == %d }.balance"

        );
    }

    public int getAccountID() {
        return get(
                "/api/v1/customer/profile",
                "accounts[0].id"

        );

    }

    public String getAccountName() {
        return get(
                "/api/v1/customer/profile'",
                "name"
        );
    }

}

