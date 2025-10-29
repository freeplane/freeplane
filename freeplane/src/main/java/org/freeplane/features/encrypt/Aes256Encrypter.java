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

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IEncrypter;

/**
 * AES-256 encryption implementation using PBKDF2 with HMAC-SHA256.
 * This provides significantly stronger encryption than the legacy DES implementation.
 * 
 * Algorithm: PBEWithHmacSHA256AndAES_256
 * - Key derivation: PBKDF2 with HMAC-SHA256
 * - Encryption: AES-256 in CBC mode
 * - Salt length: 16 bytes (128 bits)
 * - Iterations: 100,000 (significantly stronger than legacy 19 iterations)
 * 
 * @author Freeplane team
 */
public class Aes256Encrypter implements IEncrypter {
	private static final int SALT_LENGTH = 16;  // 128 bits for AES
	private static final String SALT_PRESENT_INDICATOR = " ";
	private static final String ALGORITHM = "PBEWithHmacSHA256AndAES_256";
	private static final int ITERATION_COUNT = 100000;  // OWASP recommended minimum
	
	// Version marker to distinguish from DES encryption
	private static final String VERSION_MARKER = "AES256:";
	
	private Cipher dcipher;
	private Cipher ecipher;
	private byte[] mSalt;
	private char[] passPhrase;
	private final SecureRandom secureRandom;

	public Aes256Encrypter(final StringBuilder pPassPhrase) {
		passPhrase = new char[pPassPhrase.length()];
		pPassPhrase.getChars(0, passPhrase.length, passPhrase, 0);
		secureRandom = new SecureRandom();
		// Initialize with default salt (will be replaced during encryption)
		mSalt = new byte[SALT_LENGTH];
	}

	@Override
	public String decrypt(String str) {
		if (str == null) {
			return null;
		}
		try {
			// Remove version marker if present
			if (str.startsWith(VERSION_MARKER)) {
				str = str.substring(VERSION_MARKER.length());
			}
			
			byte[] salt = null;
			final int indexOfSaltIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
			if (indexOfSaltIndicator >= 0) {
				final String saltString = str.substring(0, indexOfSaltIndicator);
				str = str.substring(indexOfSaltIndicator + 1);
				salt = DesEncrypter.fromBase64(saltString);
			}
			final byte[] dec = DesEncrypter.fromBase64(str);
			init(salt);
			if (dcipher == null) {
				return null;
			}
			final byte[] utf8 = dcipher.doFinal(dec);
			return new String(utf8, "UTF-8");
		}
		catch (final javax.crypto.BadPaddingException e) {
			LogUtils.warn("Decryption failed: bad padding", e);
		}
		catch (final IllegalBlockSizeException e) {
			LogUtils.warn("Decryption failed: illegal block size", e);
		}
		catch (final UnsupportedEncodingException e) {
			LogUtils.warn("Decryption failed: unsupported encoding", e);
		}
		catch (final IllegalArgumentException e) {
			LogUtils.warn("Decryption failed: illegal argument", e);
		}
		return null;
	}

	@Override
	public String encrypt(final String str) {
		try {
			initWithNewSalt();
			if (ecipher == null) {
				return null;
			}
			final byte[] utf8 = str.getBytes("UTF-8");
			final byte[] enc = ecipher.doFinal(utf8);
			// Include version marker to identify AES-256 encrypted content
			return VERSION_MARKER + DesEncrypter.toBase64(mSalt) + SALT_PRESENT_INDICATOR + DesEncrypter.toBase64(enc);
		}
		catch (final javax.crypto.BadPaddingException e) {
			LogUtils.severe("Encryption failed: bad padding", e);
		}
		catch (final IllegalBlockSizeException e) {
			LogUtils.severe("Encryption failed: illegal block size", e);
		}
		catch (final UnsupportedEncodingException e) {
			LogUtils.severe("Encryption failed: unsupported encoding", e);
		}
		return null;
	}

	private void initWithNewSalt() {
		final byte[] newSalt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(newSalt);
		init(newSalt);
	}

	private void init(final byte[] salt) {
		if (ecipher != null && mSalt != null && !Arrays.equals(mSalt, salt)) {
			ecipher = null;
			dcipher = null;
		}
		if (salt != null) {
			mSalt = salt;
		}
		if (ecipher == null) {
			try {
				final KeySpec keySpec = new PBEKeySpec(passPhrase, mSalt, ITERATION_COUNT);
				final SecretKey key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);
				
				ecipher = Cipher.getInstance(ALGORITHM);
				dcipher = Cipher.getInstance(ALGORITHM);
				
				final AlgorithmParameterSpec paramSpec = new PBEParameterSpec(mSalt, ITERATION_COUNT);
				ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
				dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			}
			catch (final java.security.InvalidAlgorithmParameterException e) {
				LogUtils.severe("Failed to initialize AES-256 cipher: invalid algorithm parameter", e);
			}
			catch (final java.security.spec.InvalidKeySpecException e) {
				LogUtils.severe("Failed to initialize AES-256 cipher: invalid key spec", e);
			}
			catch (final javax.crypto.NoSuchPaddingException e) {
				LogUtils.severe("Failed to initialize AES-256 cipher: no such padding", e);
			}
			catch (final java.security.NoSuchAlgorithmException e) {
				LogUtils.severe("Failed to initialize AES-256 cipher: algorithm not available. " +
						"This may require Java Cryptography Extension (JCE) Unlimited Strength.", e);
			}
			catch (final java.security.InvalidKeyException e) {
				LogUtils.severe("Failed to initialize AES-256 cipher: invalid key", e);
			}
		}
	}
	
	/**
	 * Check if the encrypted string was created with AES-256 encryption.
	 */
	public static boolean isAes256Encrypted(String encryptedString) {
		return encryptedString != null && encryptedString.startsWith(VERSION_MARKER);
	}
}

