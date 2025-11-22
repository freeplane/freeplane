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

	@Test
	public void encryptedContentHasVersionMarker() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		assertThat(encrypted, notNullValue());
		assertTrue("Encrypted content should start with prefix", 
			encrypted.startsWith(EncryptionHeader.PREFIX_AES256));
	}


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
		
		assertTrue("Encrypted content should be longer", encrypted.length() > plaintext.length());
	}

	@Test
	public void encryptedContentContainsThreeParts() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertTrue("Encrypted content should start with prefix", 
			encrypted.startsWith(EncryptionHeader.PREFIX_AES256));
		
		String base64Data = EncryptionHeader.stripPrefix(encrypted);
		assertThat("Should have base64 data after prefix", base64Data, notNullValue());
		
		final byte[] decoded = DesEncrypter.fromBase64(base64Data);
		
		// Format: 16-byte salt + 16-byte IV + ciphertext
		assertTrue("Encrypted content should have salt + IV + ciphertext", 
			decoded.length >= 48);
	}

	@Test
	public void newFormatUsesPlainTextPrefix() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		assertTrue("Encryption should use prefix",
			encrypted.startsWith("FP-AES256-V1:"));
	}
}

