package Generators;

import java.util.stream.Stream;

public class InvalidJsonPayloads {

    public static Stream<String> invalidJsonPayloads() {
        return Stream.of(
                "{",
                "[",
                "{\"id\":}",
                "{\"balance\":}",
                "{\"senderAccountId\":}",
                "{\"receiverAccountId\":}",
                "{\"amount\":}",
                "{\"name\":}",
                "{\"name\":123}",
                "not-a-json"
        );
    }
}
