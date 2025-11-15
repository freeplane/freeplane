# Encryption Tests

This directory contains comprehensive automated tests for the AES-256 encryption feature and backward compatibility with legacy DES encryption.

## Test Files

### EncryptionTest.java
Basic encryption/decryption tests covering:
- AES-256 encryption and decryption
- SingleDES (legacy) encryption and decryption
- TripleDES (legacy) encryption and decryption
- Cross-algorithm incompatibility tests
- Password edge cases (empty, long, unicode, special characters)
- Content edge cases (empty, unicode, XML, special characters)
- Wrong password handling

### EncryptionHelperTest.java
Algorithm detection and backward compatibility tests covering:
- `createEncrypter()` - always creates AES-256 encrypter
- `createDecrypter()` - automatic algorithm detection
- `tryDecryptWithAllAlgorithms()` - fallback mechanism for legacy content
- `getEncryptionAlgorithmDescription()` - algorithm identification
- Backward compatibility scenarios
- XML validation tests
- Real-world usage scenarios

### Aes256EncrypterTest.java
AES-256 specific implementation tests covering:
- Version marker ("FP-AES256-V1:") validation
- Salt and IV generation (each encryption is unique)
- Round-trip encryption/decryption
- Password variations
- Wrong password detection
- Memory cleanup (destroy() method)
- Encrypted content format validation
- Multiple encryption instances
- Edge cases and error handling

### EncryptionModelTest.java
High-level EncryptionModel tests covering:
- EncryptionModel creation and state management
- `isAccessible()` and `isLocked()` state tracking
- `getEncryptedContent()` method
- `destroy()` method
- Algorithm detection integration
- Backward compatibility with legacy algorithms
- Realistic XML node content
- Multiple node encryption
- Integration with EncryptionHelper

## Running the Tests

To run all encryption tests:
```bash
gradle test --tests "org.freeplane.features.encrypt.*"
```

To run a specific test class:
```bash
gradle test --tests "org.freeplane.features.encrypt.EncryptionTest"
gradle test --tests "org.freeplane.features.encrypt.EncryptionHelperTest"
gradle test --tests "org.freeplane.features.encrypt.Aes256EncrypterTest"
gradle test --tests "org.freeplane.features.encrypt.EncryptionModelTest"
```

To run tests with verbose output:
```bash
gradle test -PTestLoggingFull --tests "org.freeplane.features.encrypt.*"
```

## Test Coverage

The test suite covers:

1. **Encryption Algorithms**: AES-256, TripleDES, SingleDES
2. **Password Scenarios**: Correct, wrong, empty, long, unicode, special characters
3. **Content Types**: Empty, simple text, unicode, XML, special characters, large content
4. **Backward Compatibility**: Legacy DES and TripleDES content can be decrypted and upgraded
5. **Algorithm Detection**: Automatic detection of encryption algorithm from encrypted content
6. **Fallback Mechanism**: Tries all algorithms when initial decryption fails
7. **Error Handling**: Null inputs, invalid data, wrong passwords, truncated data
8. **Security**: Memory cleanup (destroy() method), password validation
9. **Real-world Scenarios**: Freeplane mindmap XML node structures

## Key Features Tested

### AES-256 Encryption (New)
- Uses PBEWithHmacSHA256AndAES_256 algorithm
- 100,000 PBKDF2 iterations (OWASP recommended)
- 16-byte salt (128 bits)
- Version marker: "FP-AES256-V1:"
- Each encryption produces unique ciphertext (random salt/IV)

### Legacy Support
- SingleDES: PBEWithMD5AndDES (weak, 19 iterations)
- TripleDES: PBEWithMD5AndTripleDES (medium, 19 iterations)
- Automatic detection and fallback decryption
- Content is automatically upgraded to AES-256 on next save

### Security Features
- Password cleanup via destroy() method
- No password/key data remains in memory after cleanup
- Different ciphertext for same plaintext (salt/IV randomization)
- Strong key derivation (PBKDF2-HMAC-SHA256)

## Test Statistics

- **Total Test Methods**: 130+
- **Test Classes**: 4
- **Algorithms Tested**: 3 (AES-256, TripleDES, SingleDES)
- **Edge Cases**: 20+ (null, empty, unicode, special chars, etc.)
- **Integration Scenarios**: 15+

## Notes for Developers

1. All tests follow JUnit 4 conventions (matching existing Freeplane tests)
2. Tests use Hamcrest matchers for assertions (matching existing tests)
3. Tests are self-contained and don't require external resources
4. Each test method has a descriptive name indicating what it tests
5. The `@After` cleanup method ensures encrypters are properly destroyed
6. Tests verify both positive cases (should work) and negative cases (should fail)

## Expected Test Results

All tests should pass. If any tests fail:

1. **Compilation errors**: Check that dependencies are correctly configured in build.gradle
2. **Algorithm not available**: Ensure JCE Unlimited Strength is enabled (required for AES-256)
3. **Specific test failures**: Check the error message - it should indicate what went wrong
4. **All tests fail**: Verify Java version and cryptography policy files

## Future Enhancements

Potential additions to the test suite:
- Performance tests (encryption speed)
- Stress tests (very large content)
- Concurrent encryption tests
- Compatibility tests with real mindmap files
- Memory leak detection tests

