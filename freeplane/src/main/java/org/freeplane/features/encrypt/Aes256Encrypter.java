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
 * AES-256-CBC encryption with PBKDF2-HMAC-SHA256 key derivation.
 * 
 * <p><b>Security Note:</b> Uses CBC mode without authenticated encryption.
 * Data integrity is NOT cryptographically guaranteed.</p>
 * 
 * @see EncryptionHeader for binary format details
 * @see EncryptionHelper
 */
public class Aes256Encrypter implements IEncrypter {
	private static final int SALT_LENGTH = 16;
	private static final int IV_LENGTH = 16;
	private static final int KEY_LENGTH = 256;
	private static final String SALT_PRESENT_INDICATOR = " ";
	private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int ITERATION_COUNT = 100000;
	private static final String OLD_VERSION_MARKER = "FP-AES256-V1:"; // Pre-binary-header format
	
	private Cipher dcipher;
	private Cipher ecipher;
	private byte[] mSalt;
	private byte[] currentIV;
	private char[] passPhrase;
	private final SecureRandom secureRandom;

	public Aes256Encrypter(final StringBuilder pPassPhrase) {
		passPhrase = new char[pPassPhrase.length()];
		pPassPhrase.getChars(0, passPhrase.length, passPhrase, 0);
		secureRandom = new SecureRandom();
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
			
			if (str.startsWith(OLD_VERSION_MARKER)) {
				str = str.substring(OLD_VERSION_MARKER.length());
				
				final int indexOfSaltIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
				if (indexOfSaltIndicator >= 0) {
					final String saltString = str.substring(0, indexOfSaltIndicator);
					str = str.substring(indexOfSaltIndicator + 1);
					salt = DesEncrypter.fromBase64(saltString);
				}
				
				final int indexOfIvIndicator = str.indexOf(SALT_PRESENT_INDICATOR);
				if (indexOfIvIndicator >= 0) {
					final String ivString = str.substring(0, indexOfIvIndicator);
					str = str.substring(indexOfIvIndicator + 1);
					iv = DesEncrypter.fromBase64(ivString);
				}
				
				ciphertext = DesEncrypter.fromBase64(str);
			} else {
				byte[] allData = DesEncrypter.fromBase64(str);
				
				if (allData != null && allData.length >= EncryptionHeader.HEADER_LENGTH) {
					EncryptionHeader header = EncryptionHeader.fromBytes(allData);
					if (header != null && header.getAlgorithm() == EncryptionHeader.Algorithm.AES256) {
						int offset = EncryptionHeader.HEADER_LENGTH;
						
						if (allData.length >= offset + SALT_LENGTH) {
							salt = Arrays.copyOfRange(allData, offset, offset + SALT_LENGTH);
							offset += SALT_LENGTH;
						}
						
						if (allData.length >= offset + IV_LENGTH) {
							iv = Arrays.copyOfRange(allData, offset, offset + IV_LENGTH);
							offset += IV_LENGTH;
						}
						
						if (allData.length > offset) {
							ciphertext = Arrays.copyOfRange(allData, offset, allData.length);
						}
					}
				}
			}
			
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
			
			EncryptionHeader header = new EncryptionHeader(EncryptionHeader.Algorithm.AES256);
			byte[] headerBytes = header.toBytes();
			
			byte[] fullData = new byte[headerBytes.length + mSalt.length + currentIV.length + enc.length];
			int offset = 0;
			System.arraycopy(headerBytes, 0, fullData, offset, headerBytes.length);
			offset += headerBytes.length;
			System.arraycopy(mSalt, 0, fullData, offset, mSalt.length);
			offset += mSalt.length;
			System.arraycopy(currentIV, 0, fullData, offset, currentIV.length);
			offset += currentIV.length;
			System.arraycopy(enc, 0, fullData, offset, enc.length);
			
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
		if (mSalt != null && salt != null && !Arrays.equals(mSalt, salt)) {
			ecipher = null;
			dcipher = null;
		}
		if (salt != null) {
			mSalt = salt;
		}
		
		final boolean needsEncryptionInit = (iv == null && ecipher == null);
		final boolean needsDecryptionInit = (iv != null && dcipher == null);
		
		if (needsEncryptionInit || needsDecryptionInit) {
			try {
				final KeySpec keySpec = new PBEKeySpec(passPhrase, mSalt, ITERATION_COUNT, KEY_LENGTH);
				final SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
				final SecretKey tmpKey = factory.generateSecret(keySpec);
				final SecretKey key = new SecretKeySpec(tmpKey.getEncoded(), "AES");
				
				if (iv == null) {
					currentIV = new byte[IV_LENGTH];
					secureRandom.nextBytes(currentIV);
					final IvParameterSpec ivSpec = new IvParameterSpec(currentIV);
					ecipher = Cipher.getInstance(CIPHER_ALGORITHM);
					ecipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
				} else {
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
	
	public static boolean isAes256Encrypted(String encryptedString) {
		if (encryptedString == null) {
			return false;
		}
		return encryptedString.startsWith(OLD_VERSION_MARKER) 
			|| EncryptionHeader.detectAlgorithm(encryptedString) == EncryptionHeader.Algorithm.AES256;
	}
	
	/**
	 * Zeroes sensitive data from memory to prevent password/key exposure.
	 */
	@Override
	public void destroy() {
		if (passPhrase != null) {
			Arrays.fill(passPhrase, '\0');
			passPhrase = null;
		}
		if (mSalt != null) {
			Arrays.fill(mSalt, (byte) 0);
			mSalt = null;
		}
		if (currentIV != null) {
			Arrays.fill(currentIV, (byte) 0);
			currentIV = null;
		}
		ecipher = null;
		dcipher = null;
	}
}

