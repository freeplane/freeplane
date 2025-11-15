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
 * <h3>Cryptographic Parameters:</h3>
 * <ul>
 * <li>Key Derivation: PBKDF2-HMAC-SHA256 (supports Unicode passwords)</li>
 * <li>Encryption: AES-256 in CBC mode with PKCS5 padding</li>
 * <li>Salt length: 16 bytes (128 bits)</li>
 * <li>IV length: 16 bytes (128 bits)</li>
 * <li>Iterations: 100,000 (OWASP recommended minimum)</li>
 * </ul>
 * 
 * <h3>Encrypted Data Format:</h3>
 * <pre>
 * [8 bytes: Binary header - magic "FPM\x01" + algorithm "AES2"]
 * [16 bytes: Salt]
 * [16 bytes: IV]
 * [N bytes: Ciphertext]
 * → Base64 encoded
 * </pre>
 * 
 * <p><b>Security Note:</b> This implementation uses CBC mode and does not provide
 * authenticated encryption or tamper detection. Data integrity/authenticity is NOT
 * cryptographically guaranteed. The primary security improvement over legacy DES is
 * the much stronger 256-bit key size and modern key derivation function that supports
 * Unicode passwords.</p>
 * 
 * <p><b>Backward Compatibility:</b> Can decrypt data encrypted with the old text marker
 * format ("FP-AES256-V1:"). All new encryptions use the binary header format.</p>
 * 
 * @author Freeplane team
 * @since 1.12.x
 * @see EncryptionHeader
 * @see EncryptionHelper
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
	 * Old version marker format for backward compatibility: FP-AES256-V1:
	 * This is kept to allow decryption of files created before the binary header format.
	 */
	private static final String OLD_VERSION_MARKER = "FP-AES256-V1:";
	
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
			byte[] salt = null;
			byte[] iv = null;
			byte[] ciphertext = null;
			
			// Check for old text-based version marker (backward compatibility)
			if (str.startsWith(OLD_VERSION_MARKER)) {
				// Old format: "FP-AES256-V1:<base64_salt> <base64_iv> <base64_ciphertext>"
				str = str.substring(OLD_VERSION_MARKER.length());
				
				// Extract salt
				final int indexOfSaltIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
				if (indexOfSaltIndicator >= 0) {
					final String saltString = str.substring(0, indexOfSaltIndicator);
					str = str.substring(indexOfSaltIndicator + 1);
					salt = DesEncrypter.fromBase64(saltString);
				}
				
				// Extract IV
				final int indexOfIvIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
				if (indexOfIvIndicator >= 0) {
					final String ivString = str.substring(0, indexOfIvIndicator);
					str = str.substring(indexOfIvIndicator + 1);
					iv = DesEncrypter.fromBase64(ivString);
				}
				
				ciphertext = DesEncrypter.fromBase64(str);
			} else {
				// Try to decode as base64 first
				byte[] allData = DesEncrypter.fromBase64(str);
				
				// Check for new binary header format
				if (allData != null && allData.length >= EncryptionHeader.HEADER_LENGTH) {
					EncryptionHeader header = EncryptionHeader.fromBytes(allData);
					if (header != null && header.getAlgorithm() == EncryptionHeader.Algorithm.AES256) {
						// New binary format: header (8 bytes) + salt (16 bytes) + IV (16 bytes) + ciphertext
						int offset = EncryptionHeader.HEADER_LENGTH;
						
						// Extract salt
						if (allData.length >= offset + SALT_LENGTH) {
							salt = Arrays.copyOfRange(allData, offset, offset + SALT_LENGTH);
							offset += SALT_LENGTH;
						}
						
						// Extract IV
						if (allData.length >= offset + IV_LENGTH) {
							iv = Arrays.copyOfRange(allData, offset, offset + IV_LENGTH);
							offset += IV_LENGTH;
						}
						
						// Extract ciphertext (remaining data)
						if (allData.length > offset) {
							ciphertext = Arrays.copyOfRange(allData, offset, allData.length);
						}
					}
				}
			}
			
			// Decrypt
			if (salt != null && iv != null && ciphertext != null) {
				init(salt, iv);
				if (dcipher == null) {
					return null;
				}
				final byte[] utf8 = dcipher.doFinal(ciphertext);
				return new String(utf8, StandardCharsets.UTF_8);
			}
			
			return null;
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
			
			// Create binary header
			EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
			byte[] headerBytes = header.toBytes();
			
			// Combine raw binary data: header + salt + IV + ciphertext
			byte[] fullData = new byte[headerBytes.length + mSalt.length + currentIV.length + enc.length];
			int offset = 0;
			System.arraycopy(headerBytes, 0, fullData, offset, headerBytes.length);
			offset += headerBytes.length;
			System.arraycopy(mSalt, 0, fullData, offset, mSalt.length);
			offset += mSalt.length;
			System.arraycopy(currentIV, 0, fullData, offset, currentIV.length);
			offset += currentIV.length;
			System.arraycopy(enc, 0, fullData, offset, enc.length);
			
			// Encode the full data (header + salt + IV + ciphertext) to base64
			return DesEncrypter.toBase64(fullData);
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
	 * Supports both old text-based marker and new binary header format.
	 */
	public static boolean isAes256Encrypted(String encryptedString) {
		if (encryptedString == null) {
			return false;
		}
		
		// Check old text-based marker (backward compatibility)
		if (encryptedString.startsWith(OLD_VERSION_MARKER)) {
			return true;
		}
		
		// Check new binary header format
		EncryptionHeader.Algorithm algorithm = EncryptionHeader.detectAlgorithm(encryptedString);
		return algorithm == EncryptionHeader.Algorithm.AES256;
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

