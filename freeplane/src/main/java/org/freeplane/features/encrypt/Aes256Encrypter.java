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
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IEncrypter;

/**
 * AES-256 encryption implementation using PBKDF2 with HMAC-SHA256.
 * This provides significantly stronger encryption than the legacy DES implementation.
 * 
 * Key Derivation: PBKDF2 with HMAC-SHA256 (supports Unicode passwords)
 * Encryption: AES-256 in CBC mode (does NOT provide authenticated encryption)
 * - Salt length: 16 bytes (128 bits)
 * - IV length: 16 bytes (128 bits)
 * - Iterations: 100,000 (significantly stronger than legacy 19 iterations)
 * 
 * Note: This implementation uses CBC mode and does not provide authenticated encryption
 * or tamper detection. Data integrity/authenticity is NOT cryptographically guaranteed.
 * The primary security improvement over legacy DES is the much stronger 256-bit key size
 * and modern key derivation function (PBKDF2-HMAC-SHA256) that supports Unicode passwords.
 * 
 * @author Freeplane team
 */
public class Aes256Encrypter implements IEncrypter {
	private static final int SALT_LENGTH = 16;  // 128 bits
	private static final int IV_LENGTH = 16;  // 128 bits for AES block size
	private static final int KEY_LENGTH = 256;  // 256 bits for AES-256
	private static final String SALT_PRESENT_INDICATOR = " ";
	private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int ITERATION_COUNT = 100000;  // OWASP recommended minimum
	
	/**
	 * Version marker format: FP-AES256-V1:
	 * - FP: Freeplane identifier
	 * - AES256: Algorithm identifier
	 * - V1: Version number for future compatibility
	 * - Chosen to be highly unlikely to appear in base64-encoded legacy data
	 */
	private static final String VERSION_MARKER = "FP-AES256-V1:";
	
	private Cipher dcipher;
	private Cipher ecipher;
	private byte[] mSalt;
	private byte[] currentIV;  // Store IV for current encryption/decryption
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
			
			// Extract salt
			byte[] salt = null;
			final int indexOfSaltIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
			if (indexOfSaltIndicator >= 0) {
				final String saltString = str.substring(0, indexOfSaltIndicator);
				str = str.substring(indexOfSaltIndicator + 1);
				salt = DesEncrypter.fromBase64(saltString);
			}
			
			// Extract IV
			byte[] iv = null;
			final int indexOfIvIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
			if (indexOfIvIndicator >= 0) {
				final String ivString = str.substring(0, indexOfIvIndicator);
				str = str.substring(indexOfIvIndicator + 1);
				iv = DesEncrypter.fromBase64(ivString);
			}
			
			final byte[] dec = DesEncrypter.fromBase64(str);
			init(salt, iv);
			if (dcipher == null) {
				return null;
			}
			final byte[] utf8 = dcipher.doFinal(dec);
			return new String(utf8, StandardCharsets.UTF_8);
		}
		catch (final javax.crypto.BadPaddingException e) {
			LogUtils.warn("Decryption failed: bad padding", e);
		}
		catch (final IllegalBlockSizeException e) {
			LogUtils.warn("Decryption failed: illegal block size", e);
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
			if (ecipher == null || currentIV == null) {
				return null;
			}
			final byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
			final byte[] enc = ecipher.doFinal(utf8);
			// Include version marker, salt, IV, and ciphertext
			return VERSION_MARKER + DesEncrypter.toBase64(mSalt) + SALT_PRESENT_INDICATOR + 
				   DesEncrypter.toBase64(currentIV) + SALT_PRESENT_INDICATOR + 
				   DesEncrypter.toBase64(enc);
		}
		catch (final javax.crypto.BadPaddingException e) {
			LogUtils.severe("Encryption failed: bad padding", e);
		}
		catch (final IllegalBlockSizeException e) {
			LogUtils.severe("Encryption failed: illegal block size", e);
		}
		return null;
	}

	private void initWithNewSalt() {
		final byte[] newSalt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(newSalt);
		init(newSalt, null);
	}

	private void init(final byte[] salt, final byte[] iv) {
		// Reset ciphers if salt has changed
		if (mSalt != null && salt != null && !Arrays.equals(mSalt, salt)) {
			ecipher = null;
			dcipher = null;
		}
		if (salt != null) {
			mSalt = salt;
		}
		
		// Check if we need to initialize based on the mode
		final boolean needsEncryptionInit = (iv == null && ecipher == null);
		final boolean needsDecryptionInit = (iv != null && dcipher == null);
		
		if (needsEncryptionInit || needsDecryptionInit) {
			try {
				// Use PBKDF2 to derive a 256-bit key from the password
				// This supports Unicode passwords unlike PBE algorithms
				final KeySpec keySpec = new PBEKeySpec(passPhrase, mSalt, ITERATION_COUNT, KEY_LENGTH);
				final SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
				final SecretKey tmpKey = factory.generateSecret(keySpec);
				final SecretKey key = new SecretKeySpec(tmpKey.getEncoded(), "AES");
				
				if (iv == null) {
					// Encryption mode: generate a new random IV
					currentIV = new byte[IV_LENGTH];
					secureRandom.nextBytes(currentIV);
					final IvParameterSpec ivSpec = new IvParameterSpec(currentIV);
					ecipher = Cipher.getInstance(CIPHER_ALGORITHM);
					ecipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
				} else {
					// Decryption mode: use the provided IV
					currentIV = iv;
					final IvParameterSpec ivSpec = new IvParameterSpec(currentIV);
					dcipher = Cipher.getInstance(CIPHER_ALGORITHM);
					dcipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
				}
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
	
	/**
	 * Clean up sensitive data from memory.
	 * This is a critical security measure to prevent passwords from remaining
	 * in memory longer than necessary.
	 */
	@Override
	public void destroy() {
		// Zero out the password
		if (passPhrase != null) {
			Arrays.fill(passPhrase, '\0');
			passPhrase = null;
		}
		
		// Zero out the salt
		if (mSalt != null) {
			Arrays.fill(mSalt, (byte) 0);
			mSalt = null;
		}
		
		// Zero out the IV
		if (currentIV != null) {
			Arrays.fill(currentIV, (byte) 0);
			currentIV = null;
		}
		
		// Clear cipher references to allow garbage collection
		ecipher = null;
		dcipher = null;
	}
}

