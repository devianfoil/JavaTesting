package requests.Skeleton;

import requests.Skeleton.Requesters.CrudRequester;

public class ProfileDataHelper {

    private final CrudRequester crud;

    public ProfileDataHelper(CrudRequester crud) {
        this.crud = crud;
    }

    public int getAccountId() {
        return crud.get()
                .extract()
                .path("accounts[0].id");
    }

    public double getBalance(int accountId) {
        return crud.get()
                .extract()
                .path(
                        "accounts.find { it.id == %d }.balance".formatted(accountId)
                );
    }

    public String getName() {
        return crud.get()
                .extract()
                .path("name");
    }
}
