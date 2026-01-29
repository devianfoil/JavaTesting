package Generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Field;

public final class RandomModelGenerator {

    private RandomModelGenerator() {
        // utility class
    }

    public static <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                if (rule == null) {
                    continue; //
                }

                if (!field.getType().equals(String.class)) {
                    throw new IllegalStateException(
                            "@GeneratingRule can be used only with String fields. " +
                                    "Field: " + clazz.getSimpleName() + "." + field.getName()
                    );
                }

                String generatedValue = new RgxGen(rule.regex()).generate();
                field.set(instance, generatedValue);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate model for class: " + clazz.getSimpleName(),
                    e
            );
        }
    }
}
