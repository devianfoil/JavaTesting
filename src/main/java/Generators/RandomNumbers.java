package Generators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class RandomNumbers {

    private RandomNumbers() {}

    public static double money(double minInclusive, double maxInclusive) {
        double raw = ThreadLocalRandom.current().nextDouble(minInclusive, Math.nextUp(maxInclusive));
        return round2(raw);
    }

    public static double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
