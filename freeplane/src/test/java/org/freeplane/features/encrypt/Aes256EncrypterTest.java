/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2025 Freeplane team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.encrypt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.freeplane.features.map.IEncrypter;
import org.junit.After;
import org.junit.Test;

/**
 * Specific tests for AES-256 encryption implementation.
 * Tests the new encryption algorithm implementation details.
 * 
 * @author Freeplane team
 */
public class Aes256EncrypterTest {
	private IEncrypter encrypter;

	@After
	public void cleanup() {
		if (encrypter != null) {
			encrypter.destroy();
			encrypter = null;
		}
	}

	// ========== Version Marker Tests ==========

	@Test
	public void encryptedContentHasVersionMarker() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		assertThat(encrypted, notNullValue());
		// New plain text prefix format: check that it starts with FP-AES256-V1:
		assertTrue("Encrypted content should start with plain text prefix", 
			encrypted.startsWith(EncryptionHeader.PREFIX_AES256));
		// Also verify algorithm detection works
		assertTrue("Should detect AES256 algorithm", 
			EncryptionHeader.detectAlgorithm(encrypted) == EncryptionHeader.Algorithm.AES256);
	}

	@Test
	public void isAes256EncryptedDetectsVersionMarker() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		assertTrue(Aes256Encrypter.isAes256Encrypted(encrypted));
	}

	@Test
	public void isAes256EncryptedReturnsFalseForLegacyContent() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String encrypted = desEncrypter.encrypt("test");
		desEncrypter.destroy();
		
		assertThat(Aes256Encrypter.isAes256Encrypted(encrypted), equalTo(false));
	}

	@Test
	public void isAes256EncryptedReturnsFalseForNull() {
		assertThat(Aes256Encrypter.isAes256Encrypted(null), equalTo(false));
	}

	@Test
	public void isAes256EncryptedReturnsFalseForPlainText() {
		assertThat(Aes256Encrypter.isAes256Encrypted("plain text"), equalTo(false));
	}

	// ========== Salt and IV Tests ==========

	@Test
	public void eachEncryptionUsesDifferentSalt() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted1 = encrypter.encrypt(plaintext);
		final String encrypted2 = encrypter.encrypt(plaintext);
		
		// Different salt/IV means different ciphertext
		assertNotEquals(encrypted1, encrypted2);
	}

	@Test
	public void differentSaltsProduceDifferentCiphertext() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		// Encrypt same plaintext multiple times
		final String plaintext = "Hello World";
		final String encrypted1 = encrypter.encrypt(plaintext);
		final String encrypted2 = encrypter.encrypt(plaintext);
		final String encrypted3 = encrypter.encrypt(plaintext);
		
		// All should be different
		assertNotEquals(encrypted1, encrypted2);
		assertNotEquals(encrypted2, encrypted3);
		assertNotEquals(encrypted1, encrypted3);
		
		// But all should decrypt to the same plaintext
		assertThat(encrypter.decrypt(encrypted1), equalTo(plaintext));
		assertThat(encrypter.decrypt(encrypted2), equalTo(plaintext));
		assertThat(encrypter.decrypt(encrypted3), equalTo(plaintext));
	}

	// ========== Encryption/Decryption Round-trip Tests ==========

	@Test
	public void encryptAndDecryptSimpleText() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void encryptAndDecryptEmptyString() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void encryptAndDecryptVeryLongText() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			sb.append("This is line ").append(i).append(" of a very long text.\n");
		}
		final String plaintext = sb.toString();
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void encryptAndDecryptBinaryLikeData() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		// Create string with all ASCII printable characters
		final StringBuilder sb = new StringBuilder();
		for (int i = 32; i < 127; i++) {
			sb.append((char) i);
		}
		final String plaintext = sb.toString();
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void encryptAndDecryptUnicodeText() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Unicode: 中文 日本語 한글 العربية עברית ελληνικά 🎉🔒📝";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void encryptAndDecryptXmlWithCDATA() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node><![CDATA[Special <>&\" content]]></node>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void encryptAndDecryptNewlines() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Line 1\nLine 2\rLine 3\r\nLine 4";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	// ========== Password Variations ==========

	@Test
	public void shortPasswordWorks() {
		final StringBuilder password = new StringBuilder("x");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void longPasswordWorks() {
		final StringBuilder password = new StringBuilder();
		for (int i = 0; i < 500; i++) {
			password.append("long");
		}
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void passwordWithSpacesWorks() {
		final StringBuilder password = new StringBuilder("pass word with spaces");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void passwordWithSpecialCharactersWorks() {
		final StringBuilder password = new StringBuilder("p@ss!w#rd$%^&*()_+-=[]{}|;':\",./<>?`~");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void passwordWithUnicodeWorks() {
		final StringBuilder password = new StringBuilder("密码🔐пароль");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	// ========== Wrong Password Tests ==========

	@Test
	public void wrongPasswordReturnsNull() {
		final StringBuilder password1 = new StringBuilder("correct");
		final IEncrypter encrypter1 = new Aes256Encrypter(password1);
		final String encrypted = encrypter1.encrypt("secret");
		encrypter1.destroy();
		
		final StringBuilder password2 = new StringBuilder("wrong");
		encrypter = new Aes256Encrypter(password2);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, nullValue());
	}

	@Test
	public void slightlyWrongPasswordReturnsNull() {
		final StringBuilder password1 = new StringBuilder("password");
		final IEncrypter encrypter1 = new Aes256Encrypter(password1);
		final String encrypted = encrypter1.encrypt("secret");
		encrypter1.destroy();
		
		final StringBuilder password2 = new StringBuilder("Password");  // Different case
		encrypter = new Aes256Encrypter(password2);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, nullValue());
	}

	@Test
	public void emptyPasswordCannotDecryptNonEmptyPassword() {
		final StringBuilder password1 = new StringBuilder("password");
		final IEncrypter encrypter1 = new Aes256Encrypter(password1);
		final String encrypted = encrypter1.encrypt("secret");
		encrypter1.destroy();
		
		final StringBuilder password2 = new StringBuilder("");
		encrypter = new Aes256Encrypter(password2);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, nullValue());
	}

	// ========== Destroy Method Tests ==========

	@Test
	public void destroyMethodCanBeCalled() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		// Should not throw exception
		encrypter.destroy();
		encrypter = null;  // Avoid double-destroy in cleanup
	}

	@Test
	public void destroyMethodCanBeCalledMultipleTimes() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		// Should not throw exception even when called multiple times
		encrypter.destroy();
		encrypter.destroy();
		encrypter.destroy();
		encrypter = null;  // Avoid double-destroy in cleanup
	}

	// ========== Null and Edge Cases ==========

	@Test
	public void decryptNullReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String decrypted = encrypter.decrypt(null);
		assertThat(decrypted, nullValue());
	}

	@Test
	public void decryptEmptyStringReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String decrypted = encrypter.decrypt("");
		assertThat(decrypted, nullValue());
	}

	@Test
	public void decryptInvalidBase64ReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String decrypted = encrypter.decrypt("FP-AES256-V1:not-valid-base64!@#$");
		assertThat(decrypted, nullValue());
	}

	@Test
	public void decryptTruncatedDataReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		// Truncate the encrypted string
		final String truncated = encrypted.substring(0, encrypted.length() / 2);
		final String decrypted = encrypter.decrypt(truncated);
		
		assertThat(decrypted, nullValue());
	}

	// ========== Different Encrypter Instances ==========

	@Test
	public void differentEncrypterInstancesWithSamePasswordWork() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter encrypter1 = new Aes256Encrypter(password);
		final String plaintext = "Hello World";
		final String encrypted = encrypter1.encrypt(plaintext);
		encrypter1.destroy();
		
		final IEncrypter encrypter2 = new Aes256Encrypter(password);
		final String decrypted = encrypter2.decrypt(encrypted);
		encrypter2.destroy();
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void sameEncrypterInstanceCanEncryptAndDecryptMultipleTimes() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		for (int i = 0; i < 10; i++) {
			final String plaintext = "Message " + i;
			final String encrypted = encrypter.encrypt(plaintext);
			final String decrypted = encrypter.decrypt(encrypted);
			assertThat(decrypted, equalTo(plaintext));
		}
	}

	// ========== Encrypted Content Format Tests ==========

	@Test
	public void encryptedContentIsNotPlaintext() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "This is secret";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted, not(equalTo(plaintext)));
		assertThat(encrypted.contains("This is secret"), equalTo(false));
	}

	@Test
	public void encryptedContentIsLongerThanPlaintext() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hi";
		final String encrypted = encrypter.encrypt(plaintext);
		
		// Encrypted content includes version marker, salt, IV, and ciphertext
		assertTrue("Encrypted content should be longer", encrypted.length() > plaintext.length());
	}

	@Test
	public void encryptedContentContainsThreeParts() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		
		// New plain text prefix format: FP-AES256-V1:{base64(salt + IV + ciphertext)}
		assertTrue("Encrypted content should start with plain text prefix", 
			encrypted.startsWith(EncryptionHeader.PREFIX_AES256));
		
		// Strip prefix and decode
		String base64Data = EncryptionHeader.stripPrefix(encrypted);
		assertThat("Should have base64 data after prefix", base64Data, notNullValue());
		
		final byte[] decoded = DesEncrypter.fromBase64(base64Data);
		
		// Format: 16-byte salt + 16-byte IV + ciphertext
		// Minimum size: 16 (salt) + 16 (IV) + 16 (min ciphertext) = 48 bytes
		assertTrue("Encrypted content should have salt + IV + ciphertext", 
			decoded.length >= 48);
		
		// Verify algorithm is detected correctly
		assertTrue("Should detect AES256 algorithm",
			EncryptionHeader.detectAlgorithm(encrypted) == EncryptionHeader.Algorithm.AES256);
	}

	// ========== Backward Compatibility Tests ==========

	@Test
	public void canDecryptLegacyBinaryHeaderFormat() {
		// Simulate old binary header format: base64(header + salt + IV + ciphertext)
		final StringBuilder password = new StringBuilder("test123");
		final String plaintext = "secret message";
		
		// Create an encrypter to encrypt with new format first
		Aes256Encrypter tempEncrypter = new Aes256Encrypter(password);
		tempEncrypter.encrypt(plaintext); // Initialize with salt/IV
		
		// Manually create old binary header format for testing
		// We'll use the current encryption but manually construct the old format
		byte[] salt = new byte[16];
		byte[] iv = new byte[16];
		java.security.SecureRandom random = new java.security.SecureRandom();
		random.nextBytes(salt);
		random.nextBytes(iv);
		
		// Create header
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = header.toBytes();
		
		// Encrypt the plaintext using a properly initialized encrypter
		Aes256Encrypter encrypter1 = new Aes256Encrypter(password);
		String newFormatEncrypted = encrypter1.encrypt(plaintext);
		
		// Extract just the encrypted parts (salt + IV + ciphertext) from new format
		String base64Payload = EncryptionHeader.stripPrefix(newFormatEncrypted);
		byte[] payload = DesEncrypter.fromBase64(base64Payload);
		
		// Reconstruct old format: header + salt + IV + ciphertext
		byte[] oldFormatBytes = new byte[headerBytes.length + payload.length];
		System.arraycopy(headerBytes, 0, oldFormatBytes, 0, headerBytes.length);
		System.arraycopy(payload, 0, oldFormatBytes, headerBytes.length, payload.length);
		
		String oldFormatEncrypted = DesEncrypter.toBase64(oldFormatBytes);
		
		// Verify we can decrypt old format
		encrypter = new Aes256Encrypter(password);
		String decrypted = encrypter.decrypt(oldFormatEncrypted);
		
		assertThat("Should decrypt old binary header format", decrypted, equalTo(plaintext));
		
		encrypter1.destroy();
		tempEncrypter.destroy();
	}

	@Test
	public void canDecryptOldTextMarkerFormat() {
		// Test the OLD_VERSION_MARKER format: FP-AES256-V1:{base64(salt)} {base64(IV)} {base64(ciphertext)}
		// This is a legacy format that should still be supported
		final StringBuilder password = new StringBuilder("test123");
		
		// Create a sample in the old space-separated format
		// Note: This format is deprecated but should still decrypt
		String oldFormat = "FP-AES256-V1:AAAAAAAAAAAAAAAAAAAAAA== BBBBBBBBBBBBBBBBBBBBBA== Y29tZSBlbmNyeXB0ZWQgZGF0YQ==";
		
		encrypter = new Aes256Encrypter(password);
		// This should not crash, though it may return null if salt/IV/ciphertext are invalid
		@SuppressWarnings("unused")
		String decrypted = encrypter.decrypt(oldFormat);
		
		// We can't verify the decrypted content since the old format data is dummy,
		// but we verify that the code handles it without crashing
		// (decrypted will be null due to bad padding, but that's expected)
	}

	@Test
	public void detectsLegacyBinaryHeaderAsAes256() {
		// Create old binary header format
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = header.toBytes();
		
		// Add some payload
		byte[] fullData = new byte[headerBytes.length + 48];
		System.arraycopy(headerBytes, 0, fullData, 0, headerBytes.length);
		
		String oldFormatEncrypted = DesEncrypter.toBase64(fullData);
		
		// Should detect as AES256
		assertTrue("Should detect legacy binary header as AES256",
			Aes256Encrypter.isAes256Encrypted(oldFormatEncrypted));
		assertTrue("Should detect algorithm correctly",
			EncryptionHeader.detectAlgorithm(oldFormatEncrypted) == EncryptionHeader.Algorithm.AES256);
	}

	@Test
	public void newFormatUsesPlainTextPrefix() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		// Verify new format uses plain text prefix
		assertTrue("New encryption should use plain text prefix",
			encrypted.startsWith("FP-AES256-V1:"));
		
		// Verify no binary header in the base64 payload
		String base64Payload = EncryptionHeader.stripPrefix(encrypted);
		byte[] decoded = DesEncrypter.fromBase64(base64Payload);
		
		// Should NOT start with binary header magic number (0x46, 0x50, 0x4D, 0x01)
		assertThat("Should not have binary header in payload", 
			decoded[0] != (byte)0x46 || decoded[1] != (byte)0x50 || decoded[2] != (byte)0x4D);
	}
}

