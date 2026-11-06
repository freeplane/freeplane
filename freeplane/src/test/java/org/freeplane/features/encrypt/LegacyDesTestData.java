package org.freeplane.features.encrypt;

final class LegacyDesTestData {
    static final String CORRECT_PASSWORD = "correct";
    static final String WRONG_PASSWORD = "wrong";
    static final String PLAINTEXT = "<node TEXT=\"test content\"/>";
    // Salt 00000000000002d8 makes WRONG_PASSWORD produce garbage with valid padding.
    static final String ENCRYPTED_CONTENT = "AAAAAAAAAtg= 7LUVCfKtAX/amA5RnElhljNX4UUqUyLp4IhrraDsUFk=";

    private LegacyDesTestData() {
    }
}
