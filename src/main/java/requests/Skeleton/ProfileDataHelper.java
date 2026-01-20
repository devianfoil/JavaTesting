package requests.Skeleton;

import requests.Skeleton.Requesters.CrudRequester;

import java.util.List;
import java.util.Map;

public class ProfileDataHelper {

    private final CrudRequester crud;

    public ProfileDataHelper(CrudRequester crud) {
        this.crud = crud;
    }

    public int getAccountId() {
        var response = crud.get().extract();
        Integer id = response.path("accounts[0].id");
        if (id == null) {
            throw new IllegalStateException("Profile response doesn't contain accounts[0].id. Response: " + response.asString());
        }
        return id;
    }

    public float getBalance(int accountId) {
        var response = crud.get().extract();
        Float balance = response.path(
                "accounts.find { it.id == %d }.balance".formatted(accountId)
        );
        if (balance == null) {
            throw new IllegalStateException("Profile response doesn't contain balance for account id=" + accountId + ". Response: " + response.asString());
        }
        return balance;
    }

    public int getAccountIdWithMaxBalance() {
        var response = crud.get().extract();
        List<Map<String, Object>> accounts = response.path("accounts");
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalStateException("Profile response doesn't contain accounts. Response: " + response.asString());
        }

        Integer bestId = null;
        double bestBalance = Double.NEGATIVE_INFINITY;

        for (Map<String, Object> account : accounts) {
            Object idObj = account.get("id");
            Object balObj = account.get("balance");
            if (!(idObj instanceof Number) || !(balObj instanceof Number)) {
                continue;
            }
            int id = ((Number) idObj).intValue();
            double bal = ((Number) balObj).doubleValue();
            if (bal > bestBalance) {
                bestBalance = bal;
                bestId = id;
            }
        }

        if (bestId == null) {
            throw new IllegalStateException("Could not determine account id with max balance. Response: " + response.asString());
        }
        return bestId;
    }

    public int getAccountIdWithMinBalanceExcluding(int excludeAccountId) {
        var response = crud.get().extract();
        List<Map<String, Object>> accounts = response.path("accounts");
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalStateException("Profile response doesn't contain accounts. Response: " + response.asString());
        }

        Integer bestId = null;
        double bestBalance = Double.POSITIVE_INFINITY;

        for (Map<String, Object> account : accounts) {
            Object idObj = account.get("id");
            Object balObj = account.get("balance");
            if (!(idObj instanceof Number) || !(balObj instanceof Number)) {
                continue;
            }
            int id = ((Number) idObj).intValue();
            if (id == excludeAccountId) {
                continue;
            }
            double bal = ((Number) balObj).doubleValue();
            if (bal < bestBalance) {
                bestBalance = bal;
                bestId = id;
            }
        }

        if (bestId == null) {
            throw new IllegalStateException("Could not determine receiver account id. Response: " + response.asString());
        }
        return bestId;
    }

    public String getName() {
        return crud.get()
                .extract()
                .path("name");
    }
}
