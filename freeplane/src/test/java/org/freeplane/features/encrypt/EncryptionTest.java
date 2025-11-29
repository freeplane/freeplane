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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Comprehensive tests for encryption functionality.
 * Tests all encryption algorithms (AES-256, TripleDES, SingleDES) and backward compatibility.
 * 
 * @author Freeplane team
 */
public class EncryptionTest {
	private IEncrypter encrypter;

	@After
	public void cleanup() {
		if (encrypter != null) {
			encrypter.destroy();
			encrypter = null;
		}
	}

	@Test
	public void aes256EncryptAndDecrypt() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256EncryptEmptyString() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256EncryptUnicodeText() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello 世界 🌍 Привет мир";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256EncryptXmlContent() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"test\" ID=\"ID_123\"><node TEXT=\"child\"/></node>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256EncryptLongText() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			sb.append("This is a long text to test encryption of large content. ");
		}
		final String plaintext = sb.toString();
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256EncryptWithSpecialCharacters() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?`~\n\t\r";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256DecryptWithWrongPassword() {
		final StringBuilder password1 = new StringBuilder("correct");
		final IEncrypter encrypter1 = new Aes256Encrypter(password1);
		
		final String plaintext = "Secret message";
		final String encrypted = encrypter1.encrypt(plaintext);
		encrypter1.destroy();
		
		final StringBuilder password2 = new StringBuilder("wrong");
		encrypter = new Aes256Encrypter(password2);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isNull();
	}

	@Test
	public void aes256EncryptedContentIsDifferent() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted).isNotNull();
		assertThat(encrypted).isNotEqualTo(plaintext);
	}

	@Test
	public void aes256EncryptedContentHasVersionMarker() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted).startsWith(EncryptionHeader.PREFIX_AES256);
	}

	@Test
	public void aes256SameContentEncryptedTwiceIsDifferent() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted1 = encrypter.encrypt(plaintext);
		final String encrypted2 = encrypter.encrypt(plaintext);
		
		// Different because of different salts/IVs
		assertThat(encrypted1).isNotEqualTo(encrypted2);
		
		// But both decrypt to the same plaintext
		assertThat(encrypter.decrypt(encrypted1)).isEqualTo(plaintext);
		assertThat(encrypter.decrypt(encrypted2)).isEqualTo(plaintext);
	}

	@Test
	public void aes256DecryptNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String decrypted = encrypter.decrypt(null);
		assertThat(decrypted).isNull();
	}

	@Test
	public void singleDesEncryptAndDecrypt() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new SingleDesEncrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void singleDesEncryptEmptyString() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new SingleDesEncrypter(password);
		
		final String plaintext = "";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void singleDesDecryptWithWrongPassword() {
		final StringBuilder password1 = new StringBuilder("correct");
		final IEncrypter encrypter1 = new SingleDesEncrypter(password1);
		
		final String plaintext = "Secret message";
		final String encrypted = encrypter1.encrypt(plaintext);
		encrypter1.destroy();
		
		final StringBuilder password2 = new StringBuilder("wrong");
		encrypter = new SingleDesEncrypter(password2);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isNull();
	}

	@Test
	public void aes256CannotDecryptSingleDesContent() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		encrypter = new Aes256Encrypter(password);
		final String decrypted = encrypter.decrypt(encrypted);
		
		// AES-256 cannot directly decrypt DES content (returns null)
		assertThat(decrypted).isNull();
	}

	@Test
	public void singleDesCannotDecryptAes256Content() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = aesEncrypter.encrypt(plaintext);
		aesEncrypter.destroy();
		
		encrypter = new SingleDesEncrypter(password);
		final String decrypted = encrypter.decrypt(encrypted);
		
		// SingleDES cannot decrypt AES-256 content (returns null)
		assertThat(decrypted).isNull();
	}

	@Test
	public void encryptWithEmptyPassword() {
		final StringBuilder password = new StringBuilder("");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptWithVeryLongPassword() {
		final StringBuilder password = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			password.append("x");
		}
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptWithUnicodePassword() {
		final StringBuilder password = new StringBuilder("密码🔒пароль");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptWithSpecialCharactersInPassword() {
		final StringBuilder password = new StringBuilder("p@$$w0rd!#%^&*()");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}
}

