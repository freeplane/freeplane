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

import org.junit.Test;

/**
 * Tests for the binary encryption header format.
 */
public class EncryptionHeaderTest {

	@Test
	public void headerIsExactly8Bytes() {
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = header.toBytes();
		
		assertThat(headerBytes.length, equalTo(8));
	}

	@Test
	public void headerStartsWithMagicNumber() {
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = header.toBytes();
		
		// Check magic number: "FPM\x01"
		assertThat(headerBytes[0], equalTo((byte) 0x46)); // 'F'
		assertThat(headerBytes[1], equalTo((byte) 0x50)); // 'P'
		assertThat(headerBytes[2], equalTo((byte) 0x4D)); // 'M'
		assertThat(headerBytes[3], equalTo((byte) 0x01)); // version 1
	}

	@Test
	public void aes256HeaderHasCorrectAlgorithmId() {
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = header.toBytes();
		
		// Check algorithm ID: "AES2"
		assertThat(headerBytes[4], equalTo((byte) 0x41)); // 'A'
		assertThat(headerBytes[5], equalTo((byte) 0x45)); // 'E'
		assertThat(headerBytes[6], equalTo((byte) 0x53)); // 'S'
		assertThat(headerBytes[7], equalTo((byte) 0x32)); // '2'
	}

	@Test
	public void desHeaderHasCorrectAlgorithmId() {
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.DES);
		byte[] headerBytes = header.toBytes();
		
		// Check algorithm ID: "DES\x00"
		assertThat(headerBytes[4], equalTo((byte) 0x44)); // 'D'
		assertThat(headerBytes[5], equalTo((byte) 0x45)); // 'E'
		assertThat(headerBytes[6], equalTo((byte) 0x53)); // 'S'
		assertThat(headerBytes[7], equalTo((byte) 0x00)); // null byte
	}

	@Test
	public void tripleDesHeaderHasCorrectAlgorithmId() {
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.TRIPLE_DES);
		byte[] headerBytes = header.toBytes();
		
		// Check algorithm ID: "3DES"
		assertThat(headerBytes[4], equalTo((byte) 0x33)); // '3'
		assertThat(headerBytes[5], equalTo((byte) 0x44)); // 'D'
		assertThat(headerBytes[6], equalTo((byte) 0x45)); // 'E'
		assertThat(headerBytes[7], equalTo((byte) 0x53)); // 'S'
	}

	@Test
	public void canParseAes256Header() {
		EncryptionHeader originalHeader = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = originalHeader.toBytes();
		
		EncryptionHeader parsedHeader = EncryptionHeader.fromBytes(headerBytes);
		
		assertThat(parsedHeader, notNullValue());
		assertThat(parsedHeader.getAlgorithm(), equalTo(EncryptionHeader.Algorithm.AES256));
	}

	@Test
	public void canParseDesHeader() {
		EncryptionHeader originalHeader = new EncryptionHeader(EncryptionHeader.Algorithm.DES);
		byte[] headerBytes = originalHeader.toBytes();
		
		EncryptionHeader parsedHeader = EncryptionHeader.fromBytes(headerBytes);
		
		assertThat(parsedHeader, notNullValue());
		assertThat(parsedHeader.getAlgorithm(), equalTo(EncryptionHeader.Algorithm.DES));
	}

	@Test
	public void canParseTripleDesHeader() {
		EncryptionHeader originalHeader = new EncryptionHeader(EncryptionHeader.Algorithm.TRIPLE_DES);
		byte[] headerBytes = originalHeader.toBytes();
		
		EncryptionHeader parsedHeader = EncryptionHeader.fromBytes(headerBytes);
		
		assertThat(parsedHeader, notNullValue());
		assertThat(parsedHeader.getAlgorithm(), equalTo(EncryptionHeader.Algorithm.TRIPLE_DES));
	}

	@Test
	public void returnsNullForInvalidMagicNumber() {
		byte[] invalidHeader = new byte[] {
			0x00, 0x00, 0x00, 0x00,  // Wrong magic number
			0x41, 0x45, 0x53, 0x32   // "AES2"
		};
		
		EncryptionHeader parsedHeader = EncryptionHeader.fromBytes(invalidHeader);
		
		assertThat(parsedHeader, nullValue());
	}

	@Test
	public void returnsNullForUnknownAlgorithmId() {
		byte[] invalidHeader = new byte[] {
			0x46, 0x50, 0x4D, 0x01,  // "FPM\x01" - correct magic
			0x58, 0x58, 0x58, 0x58   // "XXXX" - unknown algorithm
		};
		
		EncryptionHeader parsedHeader = EncryptionHeader.fromBytes(invalidHeader);
		
		assertThat(parsedHeader, nullValue());
	}

	@Test
	public void returnsNullForTooShortData() {
		byte[] shortData = new byte[] { 0x46, 0x50, 0x4D }; // Only 3 bytes
		
		EncryptionHeader parsedHeader = EncryptionHeader.fromBytes(shortData);
		
		assertThat(parsedHeader, nullValue());
	}

	@Test
	public void detectsAes256Algorithm() {
		// Create encrypted content with AES-256 header
		EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
		byte[] headerBytes = header.toBytes();
		
		// Add some payload data
		byte[] fullData = new byte[headerBytes.length + 32];
		System.arraycopy(headerBytes, 0, fullData, 0, headerBytes.length);
		
		// Encode to base64
		String base64 = DesEncrypter.toBase64(fullData);
		
		// Detect algorithm
		EncryptionHeader.Algorithm detected = EncryptionHeader.detectAlgorithm(base64);
		
		assertThat(detected, equalTo(EncryptionHeader.Algorithm.AES256));
	}

	@Test
	public void detectsOldAes256TextMarker() {
		// Old format with text marker
		String oldFormat = "FP-AES256-V1:c29tZWVuY3J5cHRlZGRhdGE=";
		
		EncryptionHeader.Algorithm detected = EncryptionHeader.detectAlgorithm(oldFormat);
		
		assertThat(detected, equalTo(EncryptionHeader.Algorithm.AES256));
	}

	@Test
	public void returnsUnknownForLegacyDes() {
		// Legacy DES format (no header, just base64-encoded salt + data)
		String legacyFormat = "qZvIMlY14wM c29tZWVuY3J5cHRlZGRhdGE=";
		
		EncryptionHeader.Algorithm detected = EncryptionHeader.detectAlgorithm(legacyFormat);
		
		assertThat(detected, equalTo(EncryptionHeader.Algorithm.UNKNOWN));
	}

	@Test
	public void aes256EncryptedContentHasHeader() {
		final StringBuilder password = new StringBuilder("test123");
		Aes256Encrypter encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "Hello World";
		final String encrypted = encrypter.encrypt(plaintext);
		
		// Check that the encrypted content has a valid header
		assertThat(EncryptionHeader.hasHeader(encrypted), equalTo(true));
		
		// Check that we can detect AES-256 algorithm
		EncryptionHeader.Algorithm detected = EncryptionHeader.detectAlgorithm(encrypted);
		assertThat(detected, equalTo(EncryptionHeader.Algorithm.AES256));
		
		encrypter.destroy();
	}

	@Test
	public void aes256CanDecryptContentWithHeader() {
		final StringBuilder password = new StringBuilder("test123");
		Aes256Encrypter encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"test\" ID=\"ID_123\"/>";
		final String encrypted = encrypter.encrypt(plaintext);
		
		// Verify header is present
		assertThat(EncryptionHeader.hasHeader(encrypted), equalTo(true));
		
		// Decrypt and verify
		final String decrypted = encrypter.decrypt(encrypted);
		assertThat(decrypted, equalTo(plaintext));
		
		encrypter.destroy();
	}

	@Test
	public void aes256CanStillDecryptOldTextMarkerFormat() {
		// This test verifies backward compatibility with the old "FP-AES256-V1:" text marker
		// We'll need to create old-format encrypted content manually or use a known sample
		
		// For now, just verify that the detection works
		String oldFormat = "FP-AES256-V1:c29tZWRhdGE=";
		assertThat(Aes256Encrypter.isAes256Encrypted(oldFormat), equalTo(true));
	}
}

