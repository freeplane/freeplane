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
 * Handles the binary header format for encrypted Freeplane map data.
 * 
 * <h3>Header Format (8 bytes):</h3>
 * <pre>
 * Bytes 0-3: Magic number "FPM\x01" (0x46504D01)
 *            - Freeplane Map encryption, version 1
 * Bytes 4-7: Algorithm ID (4-byte ASCII identifier)
 *            - "AES2" (0x41455332) = AES-256-CBC with PBKDF2-HMAC-SHA256
 *            - "DES\x00" (0x44455300) = Legacy DES (detection only)
 *            - "3DES" (0x33444553) = Legacy Triple-DES (detection only)
 *            - Reserved: "AESG" (AES-GCM), "C20P" (ChaCha20-Poly1305)
 * </pre>
 * 
 * <p>The header is prepended to raw binary encrypted data (salt + IV + ciphertext)
 * before base64 encoding. This enables trivial algorithm detection and provides
 * extensibility for future encryption algorithms.</p>
 * 
 * <p><b>Backward Compatibility:</b> Old AES-256 format with text marker "FP-AES256-V1:"
 * is still recognized for decryption. All new encryptions use the binary header format.</p>
 * 
 * <p>For detailed implementation information, see IMPLEMENTATION.md in this package.</p>
 * 
 * @author Freeplane team
 * @since 1.12.x
 * @see Aes256Encrypter
 * @see EncryptionHelper
 */
public class EncryptionHeader {
	/** Magic number: "FPM\x01" (Freeplane Map encryption, version 1) */
	private static final byte[] MAGIC_NUMBER = new byte[] {
		0x46, 0x50, 0x4D, 0x01  // "FPM\x01"
	};
	
	/** Total header length in bytes */
	public static final int HEADER_LENGTH = 8;
	
	/** Algorithm IDs */
	public static final byte[] ALGORITHM_AES256 = "AES2".getBytes(StandardCharsets.US_ASCII);
	public static final byte[] ALGORITHM_DES = new byte[] { 0x44, 0x45, 0x53, 0x00 };  // "DES\x00"
	public static final byte[] ALGORITHM_3DES = "3DES".getBytes(StandardCharsets.US_ASCII);
	
	/** Algorithm ID enum for type safety */
	public enum Algorithm {
		AES256(ALGORITHM_AES256, "AES-256-CBC with PBKDF2-HMAC-SHA256"),
		DES(ALGORITHM_DES, "Legacy DES (weak)"),
		TRIPLE_DES(ALGORITHM_3DES, "Legacy Triple-DES (medium)"),
		UNKNOWN(null, "Unknown algorithm");
		
		private final byte[] id;
		private final String description;
		
		Algorithm(byte[] id, String description) {
			this.id = id;
			this.description = description;
		}
		
		public byte[] getId() {
			return id;
		}
		
		public String getDescription() {
			return description;
		}
		
		/**
		 * Get algorithm by ID bytes.
		 */
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
	}
	
	private final Algorithm algorithm;
	
	/**
	 * Create a header with the specified algorithm.
	 */
	public EncryptionHeader(Algorithm algorithm) {
		if (algorithm == null || algorithm == Algorithm.UNKNOWN) {
			throw new IllegalArgumentException("Invalid algorithm");
		}
		this.algorithm = algorithm;
	}
	
	/**
	 * Get the algorithm.
	 */
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
	/**
	 * Create the 8-byte header.
	 */
	public byte[] toBytes() {
		byte[] header = new byte[HEADER_LENGTH];
		// Copy magic number (bytes 0-3)
		System.arraycopy(MAGIC_NUMBER, 0, header, 0, 4);
		// Copy algorithm ID (bytes 4-7)
		System.arraycopy(algorithm.getId(), 0, header, 4, 4);
		return header;
	}
	
	/**
	 * Parse a header from the first 8 bytes of encrypted data.
	 * Returns null if the data doesn't start with a valid header.
	 */
	public static EncryptionHeader fromBytes(byte[] data) {
		if (data == null || data.length < HEADER_LENGTH) {
			return null;
		}
		
		// Check magic number
		byte[] magicBytes = Arrays.copyOfRange(data, 0, 4);
		if (!Arrays.equals(magicBytes, MAGIC_NUMBER)) {
			return null;
		}
		
		// Extract algorithm ID
		byte[] algorithmBytes = Arrays.copyOfRange(data, 4, 8);
		Algorithm algorithm = Algorithm.fromId(algorithmBytes);
		
		if (algorithm == Algorithm.UNKNOWN) {
			// Unknown algorithm ID - header is present but algorithm not recognized
			return null;
		}
		
		return new EncryptionHeader(algorithm);
	}
	
	/**
	 * Check if the base64-encoded string contains a valid header.
	 * This is a quick check that decodes the beginning of the string.
	 */
	public static boolean hasHeader(String base64String) {
		if (base64String == null || base64String.length() < 12) {
			// Need at least 12 base64 chars to encode 8 bytes of header
			return false;
		}
		
		try {
			// Decode just enough to check the header
			// We need at least 12 base64 characters to get 8 bytes
			String headerPart = base64String.substring(0, Math.min(12, base64String.length()));
			byte[] decoded = DesEncrypter.fromBase64(headerPart);
			
			if (decoded != null && decoded.length >= HEADER_LENGTH) {
				byte[] headerBytes = Arrays.copyOfRange(decoded, 0, HEADER_LENGTH);
				return fromBytes(headerBytes) != null;
			}
		} catch (Exception e) {
			// Not a valid base64 string or other error
			return false;
		}
		
		return false;
	}
	
	/**
	 * Parse a header from a base64-encoded encrypted string.
	 * Returns null if no valid header is found.
	 */
	public static EncryptionHeader fromBase64String(String base64String) {
		if (base64String == null || base64String.isEmpty()) {
			return null;
		}
		
		try {
			byte[] decoded = DesEncrypter.fromBase64(base64String);
			return fromBytes(decoded);
		} catch (Exception e) {
			// Not a valid base64 string
			return null;
		}
	}
	
	/**
	 * Detect the algorithm used in encrypted content.
	 * Returns the algorithm, or UNKNOWN if it cannot be determined.
	 * 
	 * This method handles:
	 * 1. New format with binary header
	 * 2. Old AES-256 format with text marker "FP-AES256-V1:"
	 * 3. Legacy DES/TripleDES format (no marker)
	 */
	public static Algorithm detectAlgorithm(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return Algorithm.UNKNOWN;
		}
		
		// Check for old AES-256 text marker (backward compatibility)
		if (encryptedString.startsWith("FP-AES256-V1:")) {
			return Algorithm.AES256;
		}
		
		// Check for new binary header format
		EncryptionHeader header = fromBase64String(encryptedString);
		if (header != null) {
			return header.getAlgorithm();
		}
		
		// No header found - could be legacy DES or TripleDES
		// Return UNKNOWN to trigger algorithm fallback in EncryptionHelper
		return Algorithm.UNKNOWN;
	}
	
	@Override
	public String toString() {
		return "EncryptionHeader{algorithm=" + algorithm + "}";
	}
}

