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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.freeplane.features.map.IEncrypter;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for EncryptionHelper - algorithm detection and backward compatibility.
 * 
 * @author Freeplane team
 */
public class EncryptionHelperTest {
	private IEncrypter encrypter;

	@After
	public void cleanup() {
		if (encrypter != null) {
			encrypter.destroy();
			encrypter = null;
		}
	}

	@Test
	public void createEncrypterReturnsAes256() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = EncryptionHelper.createEncrypter(password);
		
		assertThat(encrypter, notNullValue());
		assertThat(encrypter.getClass().getSimpleName(), equalTo("Aes256Encrypter"));
	}

	@Test
	public void createEncrypterCanEncryptAndDecrypt() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = EncryptionHelper.createEncrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void createDecrypterDetectsAes256() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String encrypted = aesEncrypter.encrypt("Hello World");
		aesEncrypter.destroy();
		
		encrypter = EncryptionHelper.createDecrypter(password, encrypted);
		
		assertThat(encrypter, notNullValue());
		assertThat(encrypter.getClass().getSimpleName(), equalTo("Aes256Encrypter"));
		assertThat(encrypter.decrypt(encrypted), equalTo("Hello World"));
	}

	@Test
	public void createDecrypterFallsBackToSingleDesForLegacyContent() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String encrypted = desEncrypter.encrypt("Hello World");
		desEncrypter.destroy();
		
		encrypter = EncryptionHelper.createDecrypter(password, encrypted);
		
		assertThat(encrypter, notNullValue());
		assertThat(encrypter.getClass().getSimpleName(), equalTo("SingleDesEncrypter"));
	}

	@Test
	public void createDecrypterDefaultsToAes256WhenContentIsNull() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = EncryptionHelper.createDecrypter(password, null);
		
		assertThat(encrypter, notNullValue());
		assertThat(encrypter.getClass().getSimpleName(), equalTo("Aes256Encrypter"));
	}

	@Test
	public void tryDecryptWithAllAlgorithmsDecryptsAes256() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String plaintext = "Hello World";
		final String encrypted = aesEncrypter.encrypt(plaintext);
		aesEncrypter.destroy();
		
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void tryDecryptWithAllAlgorithmsDecryptsSingleDes() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String plaintext = "<node TEXT=\"test\"/>";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void tryDecryptWithAllAlgorithmsReturnsNullForWrongPassword() {
		final StringBuilder password1 = new StringBuilder("correct");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password1);
		final String encrypted = aesEncrypter.encrypt("Secret");
		aesEncrypter.destroy();
		
		final StringBuilder password2 = new StringBuilder("wrong");
		final String decrypted = EncryptionHelper.createDecrypter(password2, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, nullValue());
	}

	@Test
	public void tryDecryptWithAllAlgorithmsReturnsNullForNull() {
		final StringBuilder password = new StringBuilder("test123");
		final String decrypted = EncryptionHelper.createDecrypter(password, null).decrypt(null);
		
		assertThat(decrypted, nullValue());
	}

	@Test
	public void tryDecryptWithAllAlgorithmsReturnsNullForInvalidData() {
		final StringBuilder password = new StringBuilder("test123");
		final String decrypted = EncryptionHelper.createDecrypter(password, "not-encrypted-data").decrypt("not-encrypted-data");
		
		assertThat(decrypted, nullValue());
	}

	@Test
	public void tryDecryptWithAllAlgorithmsHandlesEmptyEncryptedContent() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String encrypted = aesEncrypter.encrypt("");
		aesEncrypter.destroy();
		
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(""));
	}

	@Test
	public void tryDecryptWithAllAlgorithmsRejectsInvalidXml() {
		final StringBuilder password = new StringBuilder("test123");
		
		// Create a DES-encrypted content that's not valid XML
		// (This simulates what might happen with wrong password producing garbage)
		final String decrypted = EncryptionHelper.createDecrypter(password, "invalid").decrypt("invalid");
		
		// Should return null because validation fails
		assertThat(decrypted, nullValue());
	}

	@Test
	public void tryDecryptWithAllAlgorithmsAcceptsValidXml() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String validXml = "<node TEXT=\"test\"><node TEXT=\"child\"/></node>";
		final String encrypted = desEncrypter.encrypt(validXml);
		desEncrypter.destroy();
		
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(validXml));
	}

	@Test
	public void tryDecryptWithAllAlgorithmsAcceptsSelfClosingNode() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String validXml = "<node TEXT=\"test\"/>";
		final String encrypted = desEncrypter.encrypt(validXml);
		desEncrypter.destroy();
		
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(validXml));
	}

	@Test
	public void getEncryptionAlgorithmDescriptionForAes256() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String encrypted = aesEncrypter.encrypt("test");
		aesEncrypter.destroy();
		
		final String description = EncryptionHelper.getEncryptionAlgorithmDescription(encrypted);
		
		assertThat(description, equalTo("AES-256"));
	}

	@Test
	public void getEncryptionAlgorithmDescriptionForLegacyDes() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String encrypted = desEncrypter.encrypt("test");
		desEncrypter.destroy();
		
		final String description = EncryptionHelper.getEncryptionAlgorithmDescription(encrypted);
		
		assertThat(description, equalTo("DES"));
	}

	@Test
	public void getEncryptionAlgorithmDescriptionForNull() {
		final String description = EncryptionHelper.getEncryptionAlgorithmDescription(null);
		
		assertThat(description, equalTo("Unknown"));
	}

	@Test
	public void legacyContentCanBeDecryptedWithHelper() {
		final StringBuilder password = new StringBuilder("test123");
		
		// Encrypt with legacy SingleDES
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String plaintext = "<node TEXT=\"legacy content\"/>";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		// Use helper to decrypt (should work with fallback)
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void aes256ContentCanBeDecryptedWithHelper() {
		final StringBuilder password = new StringBuilder("test123");
		
		// Encrypt with AES-256
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String plaintext = "<node TEXT=\"new content\"/>";
		final String encrypted = aesEncrypter.encrypt(plaintext);
		aesEncrypter.destroy();
		
		// Use helper to decrypt (should work directly)
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}

	@Test
	public void mixedAlgorithmsCanBeDecryptedSequentially() {
		final StringBuilder password = new StringBuilder("test123");
		
		// Create content with different algorithms
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String plaintext1 = "<node TEXT=\"aes content\"/>";
		final String encrypted1 = aesEncrypter.encrypt(plaintext1);
		aesEncrypter.destroy();
		
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String plaintext2 = "<node TEXT=\"des content\"/>";
		final String encrypted2 = desEncrypter.encrypt(plaintext2);
		desEncrypter.destroy();
		
		// Decrypt both with helper
		final String decrypted1 = EncryptionHelper.createDecrypter(password, encrypted1).decrypt(encrypted1);
		final String decrypted2 = EncryptionHelper.createDecrypter(password, encrypted2).decrypt(encrypted2);
		
		assertThat(decrypted1, equalTo(plaintext1));
		assertThat(decrypted2, equalTo(plaintext2));
	}

	@Test
	public void tryDecryptWithAllAlgorithmsFallsBackToDESWhenAES256Fails() {
		final StringBuilder password = new StringBuilder("test123");
		
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String plaintext = "<node TEXT=\"legacy content\"/>";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(plaintext));
	}
	
	/**
	 * Test that ensures even when AES-256 is attempted first and fails,
	 * the fallback to legacy algorithms still works.
	 */
	@Test
	public void tryDecryptWithAllAlgorithmsTriesAllAlgorithmsWhenFirstFails() {
		final StringBuilder correctPassword = new StringBuilder("correct");
		final StringBuilder wrongPassword = new StringBuilder("wrong");
		
		// Encrypt with DES
		final IEncrypter desEncrypter = new SingleDesEncrypter(correctPassword);
		final String plaintext = "<node TEXT=\"test content\"/>";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		// First verify that wrong password fails with all algorithms
		final String decryptedWrong = EncryptionHelper.createDecrypter(wrongPassword, encrypted).decrypt(encrypted);
		assertThat(decryptedWrong, nullValue());
		
		// Then verify that correct password succeeds (tests the fallback chain)
		final String decryptedCorrect = EncryptionHelper.createDecrypter(correctPassword, encrypted).decrypt(encrypted);
		assertThat(decryptedCorrect, equalTo(plaintext));
	}

	@Test
	public void complexXmlWithMultipleNodesCanBeEncryptedAndDecrypted() {
		final StringBuilder password = new StringBuilder("myPassword123");
		encrypter = EncryptionHelper.createEncrypter(password);
		
		final String complexXml = "<node TEXT=\"Parent\" ID=\"ID_1\">" +
			"<node TEXT=\"Child 1\" ID=\"ID_2\">" +
			"<node TEXT=\"Grandchild\" ID=\"ID_3\"/>" +
			"</node>" +
			"<node TEXT=\"Child 2\" ID=\"ID_4\"/>" +
			"</node>";
		
		final String encrypted = encrypter.encrypt(complexXml);
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(complexXml));
	}

	@Test
	public void xmlWithAttributesCanBeEncryptedAndDecrypted() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = EncryptionHelper.createEncrypter(password);
		
		final String xmlWithAttributes = "<node TEXT=\"Test\" " +
			"ID=\"ID_123\" " +
			"CREATED=\"1234567890\" " +
			"MODIFIED=\"1234567891\" " +
			"POSITION=\"right\" " +
			"STYLE_REF=\"default\"/>";
		
		final String encrypted = encrypter.encrypt(xmlWithAttributes);
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(xmlWithAttributes));
	}

	@Test
	public void xmlWithSpecialCharactersInTextCanBeEncryptedAndDecrypted() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = EncryptionHelper.createEncrypter(password);
		
		final String xmlWithSpecialChars = "<node TEXT=\"Test &lt;&gt;&amp;&quot; special\"/>";
		
		final String encrypted = encrypter.encrypt(xmlWithSpecialChars);
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted, equalTo(xmlWithSpecialChars));
	}
}

