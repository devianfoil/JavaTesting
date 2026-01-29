package Generators;

import java.util.stream.Stream;

public class InvalidJsonPayloads {

    public static Stream<String> invalidJsonPayloads() {
        return Stream.of(
                // Basic JSON syntax errors
                "{",                    // Incomplete JSON object
                "[",                    // JSON array instead of object
                "}",                    // Just closing brace
                "]",                    // Just closing bracket
                "null",                 // Null value
                "undefined",            // Undefined value
                "not-a-json",           // Completely invalid JSON
                
                // Empty field values
                "{\"id\":}",            // Empty value for id
                "{\"balance\":}",       // Empty value for balance
                "{\"senderAccountId\":}", // Empty value for sender account
                "{\"receiverAccountId\":}", // Empty value for receiver account
                "{\"amount\":}",        // Empty value for amount
                "{\"name\":}",          // Empty value for name
                "{\"username\":}",      // Empty value for username
                "{\"password\":}",      // Empty value for password
                
                // Wrong data types
                "{\"name\":123}",       // Number instead of string for name
                "{\"username\":123}",   // Number instead of string for username
                "{\"password\":123}",   // Number instead of string for password
                "{\"amount\":\"abc\"}", // String instead of number for amount
                "{\"balance\":\"abc\"}", // String instead of number for balance
                "{\"senderAccountId\":\"abc\"}", // String instead of number for sender
                "{\"receiverAccountId\":\"abc\"}", // String instead of number for receiver
                "{\"id\":\"abc\"}",     // String instead of number for id
                
                // Malformed structures
                "{\"name\":null}",      // Null value for required field
                "{\"username\":null}",  // Null value for required field
                "{\"password\":null}",  // Null value for required field
                "{\"amount\":null}",    // Null value for required field
                "{\"name\":}",          // Empty string for required field
                "{\"username\":}",      // Empty string for required field
                "{\"password\":}",      // Empty string for required field
                
                // Invalid JSON with extra characters
                "{\"name\":\"test\"}",  // Valid JSON but extra comma
                "{\"name\":\"test\",}", // Trailing comma
                "{\"name\":\"test\",,}", // Double comma
                ",{\"name\":\"test\"}", // Leading comma
                
                // Nested structure errors
                "{\"user\":{\"name\":}}", // Nested empty value
                "{\"data\":{\"amount\":}}", // Nested empty value
                
                // Special characters that break JSON
                "{\"name\":\"test\"}",    // Valid but with quotes
                "{\"name\":\"test\\\"}",  // Escaped quote error
                "{\"name\":\"test\n\"}",  // Newline in string
                
                // Array instead of object
                "[{\"name\":\"test\"}]",  // Array when object expected
                "[]",                     // Empty array
                
                // Duplicate keys (should be handled by JSON parser)
                "{\"name\":\"test\",\"name\":\"test2\"}", // Duplicate keys
                
                // Extremely long values (potential buffer overflow)
                "{\"name\":\"" + "a".repeat(10000) + "\"}", // Very long string
                
                // Unicode issues
                "{\"name\":\"\\u9999\"}", // Invalid Unicode
                "{\"name\":\"\\x41\"}",   // Invalid escape sequence
                
                // Numbers with issues
                "{\"amount\":Infinity}",  // Infinity (not valid JSON)
                "{\"amount\":NaN}",        // NaN (not valid JSON)
                "{\"amount\":1.7976931348623157E+308}", // Too large number
                "{\"amount\":-1.7976931348623157E+308}" // Too small number
        );
    }
}
