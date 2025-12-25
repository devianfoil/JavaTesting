package Generator;

import net.bytebuddy.utility.RandomString;

import javax.print.attribute.standard.RequestingUserName;
import java.util.concurrent.ThreadLocalRandom;

public class RandomDataGenerator {
    private RandomDataGenerator() {}

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble(1.0, 500.0);
    }


    public static String randomUsername() {
        return RandomString.make(10);
    }
}
