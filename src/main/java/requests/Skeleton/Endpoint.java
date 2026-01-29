package requests.Skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class
    ),

    TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),

    CHANGEUSERNAME(
            "/customer/profile",
            UpdateProfileRequest.class,
            UpdateProfileResponse.class
    ),

    PROFILE_REQUESTER(
            "/customer/profile",
            null,
            ProfileResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginRequest.class,
            LoginResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
