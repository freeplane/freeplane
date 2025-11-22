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

import org.freeplane.features.map.IEncrypter;

/**
 * @author Freeplane team
 * 2025
 */
public class EncryptionHelper {
	
	public static IEncrypter createEncrypter(final StringBuilder password) {
		return new Aes256Encrypter(password);
	}
	
	public static IEncrypter createDecrypter(final StringBuilder password, final String encryptedContent) {
		if (encryptedContent == null) {
			return new Aes256Encrypter(password);
		}
		
		EncryptionHeader.Algorithm algorithm = EncryptionHeader.detectAlgorithm(encryptedContent);
		switch (algorithm) {
			case AES256:
				return new Aes256Encrypter(password);
			case TRIPLE_DES:
				return new TripleDesEncrypter(password);
			case DES:
			case UNKNOWN:
			default:
				return new SingleDesEncrypter(password);
		}
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
				return "Legacy DES (will be upgraded)";
		}
	}
	
	public static String tryDecryptWithAllAlgorithms(final StringBuilder password, final String encryptedContent) {
		if (encryptedContent == null || password == null) {
			return null;
		}
		
		EncryptionHeader.Algorithm algorithm = EncryptionHeader.detectAlgorithm(encryptedContent);
		
		if (algorithm != EncryptionHeader.Algorithm.UNKNOWN) {
			IEncrypter decrypter = createDecrypter(password, encryptedContent);
			try {
				return decrypter.decrypt(encryptedContent);
			} finally {
				decrypter.destroy();
			}
		}
		
		IEncrypter tripleDesEncrypter = new TripleDesEncrypter(password);
		try {
			String result = tripleDesEncrypter.decrypt(encryptedContent);
			if (result != null) {
				return result;
			}
		} finally {
			tripleDesEncrypter.destroy();
		}
		
		IEncrypter singleDesEncrypter = new SingleDesEncrypter(password);
		try {
			return singleDesEncrypter.decrypt(encryptedContent);
		} finally {
			singleDesEncrypter.destroy();
		}
	}
}
