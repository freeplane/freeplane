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
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * Tests for the encryption header format (plain text prefixes).
 */
public class EncryptionHeaderTest {

	@Test
	public void detectsAes256Prefix() {
		String encrypted = "FP-AES256-V1:c29tZWJhc2U2NGRhdGE=";
		
		assertThat(encrypted.startsWith(EncryptionHeader.PREFIX_AES256), equalTo(true));
	}

	@Test
	public void stripsPrefixCorrectly() {
		String encrypted = "FP-AES256-V1:c29tZWJhc2U2NGRhdGE=";
		String stripped = EncryptionHeader.stripPrefix(encrypted);
		
		assertThat(stripped, equalTo("c29tZWJhc2U2NGRhdGE="));
	}

	@Test
	public void stripPrefixReturnsNullForNonPrefixedString() {
		String encrypted = "c29tZWJhc2U2NGRhdGE=";
		String stripped = EncryptionHeader.stripPrefix(encrypted);
		
		assertThat(stripped, nullValue());
	}

	@Test
	public void legacyDesHasNoPrefix() {
		String legacyFormat = "qZvIMlY14wM c29tZWVuY3J5cHRlZGRhdGE=";
		
		assertThat(legacyFormat.startsWith(EncryptionHeader.PREFIX_AES256), equalTo(false));
	}

	@Test
	public void aes256EncryptedContentHasHeader() {
		final StringBuilder password = new StringBuilder("test123");
		Aes256Encrypter encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted.startsWith(EncryptionHeader.PREFIX_AES256), equalTo(true));
		
		encrypter.destroy();
	}

	@Test
	public void aes256CanDecryptContentWithHeader() {
		final StringBuilder password = new StringBuilder("test123");
		Aes256Encrypter encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"test\" ID=\"ID_123\"/>";
		final String encrypted = encrypter.encrypt(plaintext);
		
		final String decrypted = encrypter.decrypt(encrypted);
		assertThat(decrypted, equalTo(plaintext));
		
		encrypter.destroy();
	}
	
	@Test
	public void newEncryptedContentUsesPlainTextPrefix() {
		final StringBuilder password = new StringBuilder("test123");
		Aes256Encrypter encrypter = new Aes256Encrypter(password);
		
		final String encrypted = encrypter.encrypt("test");
		
		assertThat(encrypted.startsWith("FP-AES256-V1:"), equalTo(true));
		
		encrypter.destroy();
	}
}
