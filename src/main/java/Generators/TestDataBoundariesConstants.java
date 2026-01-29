package Generators;

import lombok.Data;

@Data
public class TestDataBoundariesConstants {


    // валидные суммы депозита
    public static final double MIN_DEPOSIT_VALUES = 0.01;
    public static final double MAX_DEPOSIT_VALUES = 4999.99;
    // невалидные
    public static final double ZERO = 0.00;
    public static final double TOO_BIG = 5000.01;

}
