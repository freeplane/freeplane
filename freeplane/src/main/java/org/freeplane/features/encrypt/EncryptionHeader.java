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

/**
 * Header for encrypted Freeplane data using plain text prefixes.
 * Format: "FP-{ALGORITHM}-V1:" followed by base64-encoded encrypted data.
 */
public class EncryptionHeader {
	public static final String PREFIX_AES256 = "FP-AES256-V1:";
	public static final String PREFIX_DES = "FP-DES-V1:";
	public static final String PREFIX_3DES = "FP-3DES-V1:";
	
	public enum Algorithm {
		AES256("AES-256-CBC with PBKDF2-HMAC-SHA256", PREFIX_AES256),
		DES("Legacy DES (weak)", PREFIX_DES),
		TRIPLE_DES("Legacy Triple-DES (medium)", PREFIX_3DES),
		UNKNOWN("Unknown algorithm", null);
		
		private final String description;
		private final String prefix;
		
		Algorithm(String description, String prefix) {
			this.description = description;
			this.prefix = prefix;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getPrefix() {
			return prefix;
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
	
	public String toPrefix() {
		return algorithm.getPrefix();
	}
	
	public static boolean hasHeader(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return false;
		}
		return Algorithm.fromPrefix(encryptedString) != Algorithm.UNKNOWN;
	}
	
	public static EncryptionHeader fromEncryptedString(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return null;
		}
		Algorithm algo = Algorithm.fromPrefix(encryptedString);
		if (algo != Algorithm.UNKNOWN) {
			return new EncryptionHeader(algo);
		}
		return null;
	}
	
	public static Algorithm detectAlgorithm(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return Algorithm.UNKNOWN;
		}
		return Algorithm.fromPrefix(encryptedString);
	}
	
	public static String stripPrefix(String encryptedString) {
		if (encryptedString == null || encryptedString.isEmpty()) {
			return null;
		}
		Algorithm algo = Algorithm.fromPrefix(encryptedString);
		if (algo != Algorithm.UNKNOWN) {
			return encryptedString.substring(algo.getPrefix().length());
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "EncryptionHeader{algorithm=" + algorithm + "}";
	}
}
