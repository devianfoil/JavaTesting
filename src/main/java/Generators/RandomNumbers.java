package Generators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class RandomNumbers {

    private RandomNumbers() {}

    public static double randomDouble(double minInclusive, double maxInclusive) {
        double raw = ThreadLocalRandom.current().nextDouble(minInclusive, Math.nextUp(maxInclusive));
        return round2(raw);
    }

    public static double moneyNegative(double minAbsInclusive, double maxAbsInclusive) {
        double value = ThreadLocalRandom.current()
                .nextDouble(minAbsInclusive, Math.nextUp(maxAbsInclusive));
        return -round2(value);
    }


    public static double randomInt(int minInclusive, int maxInclusive) {
        int raw = ThreadLocalRandom.current().nextInt(minInclusive,minInclusive + 1);
        return round2(raw);
    }



    public static double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
