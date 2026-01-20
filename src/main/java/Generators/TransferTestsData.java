package Generators;

public class TransferTestsData {

    private TransferTestsData() {}

    // ===== VALID AMOUNTS =====
    public static final double VALID_MIN = 0.01;
    public static final double VALID_MAX = 4999.99;

    // ===== INVALID AMOUNTS =====
    public static final double INVALID_ZERO = 0.0;
    public static final double INVALID_NEGATIVE = -50.0;
    public static final double INVALID_TOO_BIG = 5000.01;
    public static final double INVALID_OVER_LIMIT = 10000.0;




}
