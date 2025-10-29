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
 * Helper class for encryption/decryption with backward compatibility support.
 * 
 * This class provides:
 * - Creation of new encrypters using AES-256 (strongest available)
 * - Automatic algorithm detection for decryption
 * - Backward compatibility with legacy DES and TripleDES encrypted content
 * 
 * Migration strategy:
 * - All NEW encryptions use AES-256
 * - OLD encrypted content (DES/TripleDES) is automatically detected and decrypted
 * - When decrypted content is re-encrypted, it is automatically upgraded to AES-256
 * 
 * @author Freeplane team
 */
public class EncryptionHelper {
	
	/**
	 * Create an encrypter for NEW encryptions.
	 * Always returns AES-256 encrypter for maximum security.
	 */
	public static IEncrypter createEncrypter(final StringBuilder password) {
		return new Aes256Encrypter(password);
	}
	
	/**
	 * Create a decrypter that can handle any encryption algorithm.
	 * Automatically detects the algorithm used and returns the appropriate decrypter.
	 * 
	 * This method provides backward compatibility by:
	 * 1. Checking for AES-256 marker
	 * 2. Falling back to legacy algorithms if needed
	 */
	public static IEncrypter createDecrypter(final StringBuilder password, final String encryptedContent) {
		if (encryptedContent == null) {
			// Default to AES-256 for new content
			return new Aes256Encrypter(password);
		}
		
		// Check if content was encrypted with AES-256
		if (Aes256Encrypter.isAes256Encrypted(encryptedContent)) {
			return new Aes256Encrypter(password);
		}
		
		// Legacy content - use DES
		// Note: We'll try SingleDES first (most common), but the decrypt method
		// in EncryptionModel will handle retries with other algorithms if needed
		return new SingleDesEncrypter(password);
	}
	
	/**
	 * Try to decrypt content with multiple algorithms for maximum compatibility.
	 * Returns the decrypted content or null if decryption fails with all algorithms.
	 * 
	 * Algorithm priority:
	 * 1. AES-256 (if marker present)
	 * 2. TripleDES (stronger legacy algorithm)
	 * 3. SingleDES (weakest, most common legacy)
	 */
	public static String tryDecryptWithAllAlgorithms(final StringBuilder password, final String encryptedContent) {
		if (encryptedContent == null) {
			return null;
		}
		
		// Try AES-256 first if marker is present
		if (Aes256Encrypter.isAes256Encrypted(encryptedContent)) {
			final IEncrypter aesEncrypter = new Aes256Encrypter(password);
			try {
				final String decrypted = aesEncrypter.decrypt(encryptedContent);
				if (decrypted != null) {
					return decrypted;
				}
			} finally {
				aesEncrypter.destroy();
			}
		}
		
		// Try TripleDES (stronger legacy algorithm)
		IEncrypter tripleDesEncrypter = null;
		try {
			tripleDesEncrypter = new TripleDesEncrypter(password);
			final String decrypted = tripleDesEncrypter.decrypt(encryptedContent);
			if (decrypted != null && isValidDecryption(decrypted)) {
				LogUtils.info("Successfully decrypted with TripleDES (legacy). Content will be upgraded to AES-256 on next save.");
				return decrypted;
			}
		} catch (final Exception e) {
			// TripleDES failed, will try next algorithm
		} finally {
			if (tripleDesEncrypter != null) {
				tripleDesEncrypter.destroy();
			}
		}
		
		// Try SingleDES (most common legacy algorithm)
		IEncrypter singleDesEncrypter = null;
		try {
			singleDesEncrypter = new SingleDesEncrypter(password);
			final String decrypted = singleDesEncrypter.decrypt(encryptedContent);
			if (decrypted != null && isValidDecryption(decrypted)) {
				LogUtils.info("Successfully decrypted with SingleDES (legacy). Content will be upgraded to AES-256 on next save.");
				return decrypted;
			}
		} catch (final Exception e) {
			// SingleDES failed
		} finally {
			if (singleDesEncrypter != null) {
				singleDesEncrypter.destroy();
			}
		}
		
		LogUtils.warn("Failed to decrypt content with any available algorithm");
		return null;
	}
	
	/**
	 * Validate that the decrypted content appears to be valid XML node data.
	 * This helps prevent false positives when trying multiple algorithms.
	 * 
	 * Performs lightweight validation without full XML parsing:
	 * - Empty content is valid (empty encrypted nodes are allowed)
	 * - Must start with "<node " tag
	 * - Must have closing tag or be self-closing
	 * - Basic bracket balance check
	 */
	private static boolean isValidDecryption(final String decrypted) {
		if (decrypted == null) {
			return false;
		}
		
		// Empty string is valid (empty encrypted node)
		if (decrypted.isEmpty()) {
			return true;
		}
		
		// Should start with XML node tag
		if (!decrypted.startsWith("<node ")) {
			return false;
		}
		
		// Additional validation: check for closing tag or self-closing tag
		if (!decrypted.contains("</node>") && !decrypted.contains("/>")) {
			return false;
		}
		
		// Basic sanity check: ensure balanced angle brackets
		// This catches obvious garbage while being fast
		int openCount = 0;
		int closeCount = 0;
		for (char c : decrypted.toCharArray()) {
			if (c == '<') openCount++;
			if (c == '>') closeCount++;
		}
		if (openCount != closeCount) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get a human-readable description of the encryption algorithm used.
	 */
	public static String getEncryptionAlgorithmDescription(final String encryptedContent) {
		if (encryptedContent == null) {
			return "Unknown";
		}
		
		if (Aes256Encrypter.isAes256Encrypted(encryptedContent)) {
			return "AES-256 (Strong)";
		}
		
		// Legacy algorithms - we can't definitively tell which without trying to decrypt
		return "Legacy DES (Weak - will be upgraded)";
	}
}

