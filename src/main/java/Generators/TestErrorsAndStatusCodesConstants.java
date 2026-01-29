package Generators;

import lombok.Data;

@Data
public class TestErrorsAndStatusCodesConstants {
    public static final int OK_STATUS = 200;
    public static final int BAD_REQUEST_STATUS = 400;
    public static final int FORBIDDEN_STATUS = 403;
    public static final int INTERNAL_ERROR_STATUS = 500;
    public static final int UNAUTHORIZED_STATUS = 401;

    public static final int INVALID_ACCOUNT_ID = 9999;

    public static final String INVALID_AMOUNT_MESSAGE =
            "Invalid account or amount";

    public static final String TRANSFER_INVALID_MESSAGE =
            "insufficient funds or invalid accounts";

    public static final String EMPTY_BODY = "";

    public static final String UNAUTHORIZED_MESSAGE =
            "Unauthorized access to account";

    public static final String INTERNAL_ERROR_MESSAGE =
            "Internal Server Error";
    public static final String INVALID_NAME_MESSAGE =
            "Profile name not changed due to invalid user name.";
}
