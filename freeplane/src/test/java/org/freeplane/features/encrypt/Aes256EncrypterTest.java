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

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.IEncrypter;
import org.junit.After;
import org.junit.Test;

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
		
		assertThat(encrypted).isNotNull();
		assertThat(encrypted).startsWith(EncryptionHeader.PREFIX_AES256);
	}


	@Test
	public void eachEncryptionUsesDifferentSalt() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted1 = encrypter.encrypt(plaintext);
		final String encrypted2 = encrypter.encrypt(plaintext);
		
		assertThat(encrypted1).isNotEqualTo(encrypted2);
	}

	@Test
	public void differentSaltsProduceDifferentCiphertext() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted1 = encrypter.encrypt(plaintext);
		final String encrypted2 = encrypter.encrypt(plaintext);
		final String encrypted3 = encrypter.encrypt(plaintext);
		
		assertThat(encrypted1).isNotEqualTo(encrypted2);
		assertThat(encrypted2).isNotEqualTo(encrypted3);
		assertThat(encrypted1).isNotEqualTo(encrypted3);
		
		assertThat(encrypter.decrypt(encrypted1)).isEqualTo(plaintext);
		assertThat(encrypter.decrypt(encrypted2)).isEqualTo(plaintext);
		assertThat(encrypter.decrypt(encrypted3)).isEqualTo(plaintext);
	}

	@Test
	public void encryptAndDecryptSimpleText() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptAndDecryptEmptyString() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
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
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptAndDecryptBinaryLikeData() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final StringBuilder sb = new StringBuilder();
		for (int i = 32; i < 127; i++) {
			sb.append((char) i);
		}
		final String plaintext = sb.toString();
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptAndDecryptUnicodeText() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Unicode: 中文 日本語 한글 العربية עברית ελληνικά 🎉🔒📝";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptAndDecryptXmlWithCDATA() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node><![CDATA[Special <>&\" content]]></node>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptAndDecryptNewlines() {
		final StringBuilder password = new StringBuilder("password");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Line 1\nLine 2\rLine 3\r\nLine 4";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void shortPasswordWorks() {
		final StringBuilder password = new StringBuilder("x");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
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
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void passwordWithSpacesWorks() {
		final StringBuilder password = new StringBuilder("pass word with spaces");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void passwordWithSpecialCharactersWorks() {
		final StringBuilder password = new StringBuilder("p@ss!w#rd$%^&*()_+-=[]{}|;':\",./<>?`~");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void passwordWithUnicodeWorks() {
		final StringBuilder password = new StringBuilder("密码🔐пароль");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
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
		
		assertThat(decrypted).isNull();
	}

	@Test
	public void slightlyWrongPasswordReturnsNull() {
		final StringBuilder password1 = new StringBuilder("password");
		final IEncrypter encrypter1 = new Aes256Encrypter(password1);
		final String encrypted = encrypter1.encrypt("secret");
		encrypter1.destroy();
		
		final StringBuilder password2 = new StringBuilder("Password");
		encrypter = new Aes256Encrypter(password2);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isNull();
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
		
		assertThat(decrypted).isNull();
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
		assertThat(decrypted).isNull();
	}

	@Test
	public void decryptEmptyStringReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String decrypted = encrypter.decrypt("");
		assertThat(decrypted).isNull();
	}

	@Test
	public void decryptInvalidBase64ReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String decrypted = encrypter.decrypt("FP-AES256-V1:not-valid-base64!@#$");
		assertThat(decrypted).isNull();
	}

	@Test
	public void decryptTruncatedDataReturnsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		final String truncated = encrypted.substring(0, encrypted.length() / 2);
		final String decrypted = encrypter.decrypt(truncated);
		
		assertThat(decrypted).isNull();
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
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void sameEncrypterInstanceCanEncryptAndDecryptMultipleTimes() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		for (int i = 0; i < 10; i++) {
			final String plaintext = "Message " + i;
			final String encrypted = encrypter.encrypt(plaintext);
			final String decrypted = encrypter.decrypt(encrypted);
			assertThat(decrypted).isEqualTo(plaintext);
		}
	}

	@Test
	public void encryptedContentIsNotPlaintext() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "This is secret";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted).isNotEqualTo(plaintext);
		assertThat(encrypted).doesNotContain("This is secret");
	}

	@Test
	public void encryptedContentIsLongerThanPlaintext() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hi";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted.length()).isGreaterThan(plaintext.length());
	}

	@Test
	public void encryptedContentContainsThreeParts() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "test";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted).startsWith(EncryptionHeader.PREFIX_AES256);
		
		String base64Data = EncryptionHeader.stripPrefix(encrypted);
		assertThat(base64Data).isNotNull();
		
		final byte[] decoded = DesEncrypter.fromBase64(base64Data);
		
		assertThat(decoded.length).isGreaterThanOrEqualTo(44);
	}

	@Test
	public void newFormatUsesPlainTextPrefix() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		assertThat(encrypted).startsWith("FP-AES256-V1:");
	}
}

