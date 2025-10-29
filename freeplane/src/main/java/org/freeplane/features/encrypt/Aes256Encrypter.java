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
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
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
	private AlgorithmParameters encryptParams;  // Store parameters including IV for PBES2
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
			
			// Extract algorithm parameters (IV, etc.)
			byte[] encodedParams = null;
			final int indexOfParamsIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
			if (indexOfParamsIndicator >= 0) {
				final String paramsString = str.substring(0, indexOfParamsIndicator);
				str = str.substring(indexOfParamsIndicator + 1);
				encodedParams = DesEncrypter.fromBase64(paramsString);
			}
			
			final byte[] dec = DesEncrypter.fromBase64(str);
			init(salt, encodedParams);
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
			if (ecipher == null || encryptParams == null) {
				return null;
			}
			final byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
			final byte[] enc = ecipher.doFinal(utf8);
			final byte[] encodedParams = encryptParams.getEncoded();
			// Include version marker, salt, algorithm parameters (including IV), and ciphertext
			return VERSION_MARKER + DesEncrypter.toBase64(mSalt) + SALT_PRESENT_INDICATOR + 
				   DesEncrypter.toBase64(encodedParams) + SALT_PRESENT_INDICATOR + 
				   DesEncrypter.toBase64(enc);
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
		catch (final java.io.IOException e) {
			LogUtils.severe("Encryption failed: could not encode parameters", e);
		}
		return null;
	}

	private void initWithNewSalt() {
		final byte[] newSalt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(newSalt);
		init(newSalt, null);
	}

	private void init(final byte[] salt, final byte[] encodedParams) {
		if (ecipher != null && mSalt != null && !Arrays.equals(mSalt, salt)) {
			ecipher = null;
			dcipher = null;
		}
		if (salt != null) {
			mSalt = salt;
		}
		if (ecipher == null) {
			try {
				final KeySpec keySpec = new PBEKeySpec(passPhrase);
				final SecretKey key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);
				
				// For PBE algorithms, use PBEParameterSpec with salt and iteration count
				final PBEParameterSpec paramSpec = new PBEParameterSpec(mSalt, ITERATION_COUNT);
				
				ecipher = Cipher.getInstance(ALGORITHM);
				
				if (encodedParams == null) {
					// Encryption mode: initialize with PBEParameterSpec
					ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
					// Store the generated parameters for later use
					encryptParams = ecipher.getParameters();
				} else {
					// Decryption mode: use the stored parameters
					final AlgorithmParameters params = AlgorithmParameters.getInstance(ALGORITHM);
					params.init(encodedParams);
					dcipher = Cipher.getInstance(ALGORITHM);
					dcipher.init(Cipher.DECRYPT_MODE, key, params);
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
			catch (final java.io.IOException e) {
				LogUtils.severe("Failed to initialize AES-256 cipher: could not decode parameters", e);
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

