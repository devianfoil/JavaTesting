package Generators;

public class DepositBoundaryConstants {


    public static final int OK_STATUS = 200;
    public static final int BAD_REQUEST_STATUS = 400;
    public static final int FORBIDDEN_STATUS = 403;
    public static final int INTERNAL_ERROR_STATUS = 500;




    // валидные суммы депозита
    public static final double MIN_DEPOSIT_VALUES = 0.01;
    public static final double MAX_DEPOSIT_VALUES = 4999.99;
    // невалидные
    public static final double ZERO = 0.00;
    public static final double NEGATIVE = -100.00;
    public static final double TOO_BIG = 5000.01;
    // ===== ERROR MESSAGES =====
    public static final String INVALID_AMOUNT_MESSAGE =
            "Invalid account or amount";

    public static final String UNAUTHORIZED_MESSAGE =
            "Unauthorized access to account";

    public static final String INTERNAL_ERROR_MESSAGE =
            "Internal Server Error";
}
