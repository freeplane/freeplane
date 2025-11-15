# Binary Encryption Header Implementation

## Overview

Implemented a binary header format for encrypted Freeplane map data that makes algorithm detection trivial and extensible.

## Header Format (8 bytes)

```
Bytes 0-3: Magic number "FPM\x01" (0x46504D01)
           - F (0x46) = Freeplane
           - P (0x50) = 
           - M (0x4D) = Map encryption
           - \x01 (0x01) = Version 1

Bytes 4-7: Algorithm ID (4-byte ASCII identifier)
```

## Supported Algorithm IDs

| Algorithm   | ID Bytes      | Hex Value    | Description                           |
|-------------|---------------|--------------|---------------------------------------|
| AES-256     | "AES2"        | 0x41455332   | AES-256-CBC with PBKDF2-HMAC-SHA256   |
| DES         | "DES\x00"     | 0x44455300   | Legacy DES (detection only)           |
| Triple-DES  | "3DES"        | 0x33444553   | Legacy Triple-DES (detection only)    |

## Reserved for Future Use

- "AESG" - AES-GCM (authenticated encryption)
- "C20P" - ChaCha20-Poly1305 (authenticated encryption)
- "AES3" - AES-256 with different KDF or mode

## File Structure

### New Files

1. **EncryptionHeader.java** - Manages the 8-byte binary header format
   - Header creation and parsing
   - Algorithm detection
   - Support for all defined algorithm IDs

### Modified Files

1. **Aes256Encrypter.java** - Updated to use binary header
   - Encryption: Prepends 8-byte header to raw binary data (salt + IV + ciphertext)
   - Decryption: Supports both new binary header and old text marker ("FP-AES256-V1:")
   - Full backward compatibility maintained

2. **EncryptionHelper.java** - Updated algorithm detection
   - Uses new `EncryptionHeader.detectAlgorithm()` method
   - Provides human-readable algorithm descriptions

### Test Files

1. **EncryptionHeaderTest.java** - Comprehensive tests for header functionality
   - Header format validation
   - Algorithm ID verification  
   - Round-trip encryption/decryption
   - Backward compatibility tests

## Binary Format Details

### Encrypted Data Structure (New Format)

```
[8 bytes: Header]
[16 bytes: Salt]
[16 bytes: IV]
[N bytes: Ciphertext]
↓
Base64 encoded
```

Example encrypted data (base64):
```
RlBNAUFFUzKMnpuEz3uMbHJqibaYJ6JsOYG5M8CfFI94mStwIR...
│││││││││
│││││└─ 'S' (0x53)
│││││└─ 'E' (0x45)
││││└─ 'A' (0x41)
│││└─ '2' (0x32)
││└─ \x01 (version)
│└─ 'M'
└─ 'F' (magic number start)
```

### Old Format (Still Supported)

```
FP-AES256-V1:<base64_salt> <base64_iv> <base64_ciphertext>
```

## Backward Compatibility

The implementation maintains full backward compatibility:

1. **Reading old format**: Old "FP-AES256-V1:" text marker is still recognized
2. **Writing new format**: All new encryptions use the binary header
3. **Automatic upgrade**: When old content is decrypted and re-encrypted, it's automatically upgraded to the new format
4. **Legacy DES/TripleDES**: Still supported through algorithm fallback mechanism

## Algorithm Detection Logic

```java
// Pseudocode
if (startsWith("FP-AES256-V1:")) {
    return AES256;  // Old text marker
} else if (hasValidBinaryHeader()) {
    return detectFromHeader();  // New binary header
} else {
    return UNKNOWN;  // Legacy DES/TripleDES (requires fallback)
}
```

## Benefits

1. **Trivial Detection**: Algorithm can be determined by reading first 8 bytes
2. **Extensible**: New algorithms can be added without breaking compatibility
3. **Efficient**: No string parsing needed for algorithm detection
4. **Self-Documenting**: Magic number identifies Freeplane encrypted data
5. **Version Support**: Version byte allows future format changes
6. **Space Efficient**: Only 8 bytes overhead (vs 13+ chars for text marker)

## Testing

All tests pass successfully:
- ✅ Header format matches specification exactly
- ✅ All algorithm IDs are correct
- ✅ Round-trip encryption/decryption works
- ✅ Backward compatibility with old format maintained
- ✅ Algorithm detection works for all formats

## Example Usage

```java
// Encryption (automatically includes header)
StringBuilder password = new StringBuilder("secret");
Aes256Encrypter encrypter = new Aes256Encrypter(password);
String encrypted = encrypter.encrypt("<node TEXT=\"test\"/>");

// Detection
EncryptionHeader.Algorithm algo = EncryptionHeader.detectAlgorithm(encrypted);
System.out.println(algo);  // Output: AES256

// Decryption (automatically handles header)
String decrypted = encrypter.decrypt(encrypted);
```

## Migration Path

### Phase 1 (Current)
- ✅ Implement binary header format
- ✅ Maintain backward compatibility
- ✅ Write new format, read both formats

### Phase 2 (Future)
- Add AES-GCM support with "AESG" algorithm ID
- Add ChaCha20-Poly1305 support with "C20P" algorithm ID

### Phase 3 (Long-term)
- Deprecate legacy DES/TripleDES (read-only)
- Consider removing old text marker support (after migration period)

## Security Considerations

1. **No Information Leakage**: Header reveals algorithm but not key or password
2. **Tamper Evidence**: Magic number helps detect corrupted/malicious data
3. **Future-Proof**: Version byte allows security upgrades
4. **Algorithm Agility**: Easy to migrate to stronger algorithms

## Implementation Notes

- Header is prepended to raw binary data before base64 encoding
- Decryption tries header detection first, then falls back to legacy formats
- All sensitive data (password, salt, IV) is properly zeroed in memory after use
- Thread-safe implementation maintained

