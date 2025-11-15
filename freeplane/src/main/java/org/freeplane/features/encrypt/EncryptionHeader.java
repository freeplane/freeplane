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
 * Binary header for encrypted Freeplane data: 8 bytes = "FPM\x01" + 4-byte algorithm ID.
 * See IMPLEMENTATION.md for format details.
 */
public class EncryptionHeader {
	private static final byte[] MAGIC_NUMBER = new byte[] { 0x46, 0x50, 0x4D, 0x01 };
	public static final int HEADER_LENGTH = 8;
	public static final byte[] ALGORITHM_AES256 = "AES2".getBytes(StandardCharsets.US_ASCII);
	public static final byte[] ALGORITHM_DES = new byte[] { 0x44, 0x45, 0x53, 0x00 };
	public static final byte[] ALGORITHM_3DES = "3DES".getBytes(StandardCharsets.US_ASCII);
	
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
	
	public EncryptionHeader(Algorithm algorithm) {
		if (algorithm == null || algorithm == Algorithm.UNKNOWN) {
			throw new IllegalArgumentException("Invalid algorithm");
		}
		this.algorithm = algorithm;
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
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
	
	public static boolean hasHeader(String base64String) {
		if (base64String == null || base64String.length() < 12) {
			return false;
		}
		try {
			String headerPart = base64String.substring(0, Math.min(12, base64String.length()));
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
	
	public static Algorithm detectAlgorithm(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return Algorithm.UNKNOWN;
		}
		if (encryptedString.startsWith("FP-AES256-V1:")) {
			return Algorithm.AES256;
		}
		EncryptionHeader header = fromBase64String(encryptedString);
		if (header != null) {
			return header.getAlgorithm();
		}
		return Algorithm.UNKNOWN;
	}
	
	@Override
	public String toString() {
		return "EncryptionHeader{algorithm=" + algorithm + "}";
	}
}

