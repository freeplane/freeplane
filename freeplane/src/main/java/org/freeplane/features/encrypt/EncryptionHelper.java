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

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IEncrypter;

/**
 * Manages encryption/decryption with automatic algorithm detection.
 * All new encryptions use AES-256; legacy DES/TripleDES is auto-upgraded on re-encryption.
 */
public class EncryptionHelper {
	
	public static IEncrypter createEncrypter(final StringBuilder password) {
		return new Aes256Encrypter(password);
	}
	
	public static IEncrypter createDecrypter(final StringBuilder password, final String encryptedContent) {
		if (encryptedContent == null) {
			return new Aes256Encrypter(password);
		}
		if (Aes256Encrypter.isAes256Encrypted(encryptedContent)) {
			return new Aes256Encrypter(password);
		}
		return new SingleDesEncrypter(password);
	}
	
	/**
	 * Attempts decryption with AES-256, TripleDES, and SingleDES in sequence.
	 * Fallback ensures maximum data recovery even with corrupted headers.
	 * @return decrypted content or null if all algorithms fail
	 */
	public static String tryDecryptWithAllAlgorithms(final StringBuilder password, final String encryptedContent) {
		if (encryptedContent == null) {
			return null;
		}
		
		if (Aes256Encrypter.isAes256Encrypted(encryptedContent)) {
			final IEncrypter aesEncrypter = new Aes256Encrypter(password);
			try {
				final String decrypted = aesEncrypter.decrypt(encryptedContent);
				if (decrypted != null) {
					return decrypted;
				}
				LogUtils.info("AES-256 decryption failed despite markers being present - trying legacy algorithms as fallback");
			} finally {
				aesEncrypter.destroy();
			}
		}
		
		IEncrypter tripleDesEncrypter = null;
		try {
			tripleDesEncrypter = new TripleDesEncrypter(password);
			final String decrypted = tripleDesEncrypter.decrypt(encryptedContent);
			if (decrypted != null && isValidDecryption(decrypted)) {
				LogUtils.info("Successfully decrypted with TripleDES (legacy). Content will be upgraded to AES-256 on next save.");
				return decrypted;
			}
		} catch (final Exception e) {
		} finally {
			if (tripleDesEncrypter != null) {
				tripleDesEncrypter.destroy();
			}
		}
		
		IEncrypter singleDesEncrypter = null;
		try {
			singleDesEncrypter = new SingleDesEncrypter(password);
			final String decrypted = singleDesEncrypter.decrypt(encryptedContent);
			if (decrypted != null && isValidDecryption(decrypted)) {
				LogUtils.info("Successfully decrypted with SingleDES (legacy). Content will be upgraded to AES-256 on next save.");
				return decrypted;
			}
		} catch (final Exception e) {
		} finally {
			if (singleDesEncrypter != null) {
				singleDesEncrypter.destroy();
			}
		}
		
		LogUtils.warn("Failed to decrypt content with any available algorithm");
		return null;
	}
	
	private static boolean isValidDecryption(final String decrypted) {
		if (decrypted == null) {
			return false;
		}
		if (decrypted.isEmpty()) {
			return true;
		}
		if (!decrypted.startsWith("<node ")) {
			return false;
		}
		if (!decrypted.contains("</node>") && !decrypted.contains("/>")) {
			return false;
		}
		int openCount = 0;
		int closeCount = 0;
		for (char c : decrypted.toCharArray()) {
			if (c == '<') openCount++;
			if (c == '>') closeCount++;
		}
		return openCount == closeCount;
	}
	
	public static String getEncryptionAlgorithmDescription(final String encryptedContent) {
		if (encryptedContent == null) {
			return "Unknown";
		}
		EncryptionHeader.Algorithm algorithm = EncryptionHeader.detectAlgorithm(encryptedContent);
		switch (algorithm) {
			case AES256:
				return "AES-256 (Strong)";
			case DES:
				return "Legacy DES (Weak - will be upgraded)";
			case TRIPLE_DES:
				return "Legacy Triple-DES (Medium - will be upgraded)";
			case UNKNOWN:
			default:
				return "Legacy DES/TripleDES (will be upgraded)";
		}
	}
}

