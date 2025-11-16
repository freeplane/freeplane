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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Header for encrypted Freeplane data using plain text prefixes.
 * Format: "FP-{ALGORITHM}-V1:" followed by base64-encoded encrypted data.
 * See IMPLEMENTATION.md for format details.
 */
public class EncryptionHeader {
	// Plain text prefixes for current format
	public static final String PREFIX_AES256 = "FP-AES256-V1:";
	public static final String PREFIX_DES = "FP-DES-V1:";
	public static final String PREFIX_3DES = "FP-3DES-V1:";
	
	// Legacy binary header support (for backward compatibility)
	private static final byte[] MAGIC_NUMBER = new byte[] { 0x46, 0x50, 0x4D, 0x01 };
	public static final int HEADER_LENGTH = 8;
	public static final byte[] ALGORITHM_AES256 = "AES2".getBytes(StandardCharsets.US_ASCII);
	public static final byte[] ALGORITHM_DES = new byte[] { 0x44, 0x45, 0x53, 0x00 };
	public static final byte[] ALGORITHM_3DES = "3DES".getBytes(StandardCharsets.US_ASCII);
	
	public enum Algorithm {
		AES256(ALGORITHM_AES256, "AES-256-CBC with PBKDF2-HMAC-SHA256", PREFIX_AES256),
		DES(ALGORITHM_DES, "Legacy DES (weak)", PREFIX_DES),
		TRIPLE_DES(ALGORITHM_3DES, "Legacy Triple-DES (medium)", PREFIX_3DES),
		UNKNOWN(null, "Unknown algorithm", null);
		
		private final byte[] id;
		private final String description;
		private final String prefix;
		
		Algorithm(byte[] id, String description, String prefix) {
			this.id = id;
			this.description = description;
			this.prefix = prefix;
		}
		
		public byte[] getId() {
			return id;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public static Algorithm fromId(byte[] idBytes) {
			if (idBytes == null || idBytes.length != 4) {
				return UNKNOWN;
			}
			for (Algorithm algo : values()) {
				if (algo != UNKNOWN && Arrays.equals(algo.id, idBytes)) {
					return algo;
				}
			}
			return UNKNOWN;
		}
		
		public static Algorithm fromPrefix(String encryptedString) {
			if (encryptedString == null) {
				return UNKNOWN;
			}
			for (Algorithm algo : values()) {
				if (algo != UNKNOWN && algo.prefix != null && encryptedString.startsWith(algo.prefix)) {
					return algo;
				}
			}
			return UNKNOWN;
		}
	}
	
	private final Algorithm algorithm;
	
	public EncryptionHeader(Algorithm algorithm) {
		if (algorithm == null || algorithm == Algorithm.UNKNOWN) {
			throw new IllegalArgumentException("Invalid algorithm");
		}
		this.algorithm = algorithm;
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
	/**
	 * Returns the plain text prefix for this header (e.g., "FP-AES256-V1:").
	 */
	public String toPrefix() {
		return algorithm.getPrefix();
	}
	
	/**
	 * Legacy method: Returns binary header bytes for backward compatibility.
	 * New code should use toPrefix() instead.
	 */
	@Deprecated
	public byte[] toBytes() {
		byte[] header = new byte[HEADER_LENGTH];
		System.arraycopy(MAGIC_NUMBER, 0, header, 0, 4);
		System.arraycopy(algorithm.getId(), 0, header, 4, 4);
		return header;
	}
	
	public static EncryptionHeader fromBytes(byte[] data) {
		if (data == null || data.length < HEADER_LENGTH) {
			return null;
		}
		byte[] magicBytes = Arrays.copyOfRange(data, 0, 4);
		if (!Arrays.equals(magicBytes, MAGIC_NUMBER)) {
			return null;
		}
		byte[] algorithmBytes = Arrays.copyOfRange(data, 4, 8);
		Algorithm algorithm = Algorithm.fromId(algorithmBytes);
		if (algorithm == Algorithm.UNKNOWN) {
			return null;
		}
		return new EncryptionHeader(algorithm);
	}
	
	/**
	 * Checks if the encrypted string has a valid header (plain text prefix or legacy binary header).
	 */
	public static boolean hasHeader(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return false;
		}
		// Check for plain text prefix
		if (Algorithm.fromPrefix(encryptedString) != Algorithm.UNKNOWN) {
			return true;
		}
		// Check for legacy binary header
		if (encryptedString.length() < 12) {
			return false;
		}
		try {
			String headerPart = encryptedString.substring(0, Math.min(12, encryptedString.length()));
			byte[] decoded = DesEncrypter.fromBase64(headerPart);
			if (decoded != null && decoded.length >= HEADER_LENGTH) {
				byte[] headerBytes = Arrays.copyOfRange(decoded, 0, HEADER_LENGTH);
				return fromBytes(headerBytes) != null;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * Parses encryption header from an encrypted string.
	 * Supports both plain text prefixes and legacy binary headers.
	 */
	public static EncryptionHeader fromEncryptedString(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return null;
		}
		// Check for plain text prefix first
		Algorithm algo = Algorithm.fromPrefix(encryptedString);
		if (algo != Algorithm.UNKNOWN) {
			return new EncryptionHeader(algo);
		}
		// Fall back to legacy binary header
		try {
			byte[] decoded = DesEncrypter.fromBase64(encryptedString);
			return fromBytes(decoded);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Legacy method: parses binary header from base64 string.
	 * New code should use fromEncryptedString() instead.
	 */
	@Deprecated
	public static EncryptionHeader fromBase64String(String base64String) {
		if (base64String == null || base64String.isEmpty()) {
			return null;
		}
		try {
			byte[] decoded = DesEncrypter.fromBase64(base64String);
			return fromBytes(decoded);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Detects the encryption algorithm from an encrypted string.
	 * Supports both plain text prefixes and legacy binary headers.
	 */
	public static Algorithm detectAlgorithm(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return Algorithm.UNKNOWN;
		}
		// Check for plain text prefix
		Algorithm algo = Algorithm.fromPrefix(encryptedString);
		if (algo != Algorithm.UNKNOWN) {
			return algo;
		}
		// Fall back to legacy binary header
		EncryptionHeader header = fromBase64String(encryptedString);
		if (header != null) {
			return header.getAlgorithm();
		}
		return Algorithm.UNKNOWN;
	}
	
	/**
	 * Strips the header prefix from an encrypted string and returns the base64 payload.
	 * Returns null if no valid header is found.
	 */
	public static String stripPrefix(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return null;
		}
		// Try to strip plain text prefix
		Algorithm algo = Algorithm.fromPrefix(encryptedString);
		if (algo != Algorithm.UNKNOWN) {
			return encryptedString.substring(algo.getPrefix().length());
		}
		// No plain text prefix found - might be legacy binary header format
		return null;
	}
	
	@Override
	public String toString() {
		return "EncryptionHeader{algorithm=" + algorithm + "}";
	}
}

