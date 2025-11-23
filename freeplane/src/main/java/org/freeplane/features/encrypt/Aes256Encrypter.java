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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IEncrypter;

public class Aes256Encrypter implements IEncrypter {
	private static final int SALT_LENGTH = 16;
	private static final int IV_LENGTH = 12;
	private static final int KEY_LENGTH = 256;
	private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
	private static final int ITERATION_COUNT = 100000;
	
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
			String strippedPrefix = EncryptionHeader.stripPrefix(str);
			if (strippedPrefix == null) {
				return null;
			}
			
			byte[] allData = DesEncrypter.fromBase64(strippedPrefix);
			if (allData == null || allData.length < SALT_LENGTH + IV_LENGTH) {
				return null;
			}
			
			int offset = 0;
			byte[] salt = Arrays.copyOfRange(allData, offset, offset + SALT_LENGTH);
			offset += SALT_LENGTH;
			byte[] iv = Arrays.copyOfRange(allData, offset, offset + IV_LENGTH);
			offset += IV_LENGTH;
			
			if (allData.length <= offset) {
				return null;
			}
			byte[] ciphertext = Arrays.copyOfRange(allData, offset, allData.length);
			
			init(salt, iv);
			if (dcipher == null) {
				return null;
			}
			final byte[] utf8 = dcipher.doFinal(ciphertext);
			return new String(utf8, StandardCharsets.UTF_8);
		}
		catch (final AEADBadTagException e) {
		}
		catch (final BadPaddingException e) {
		}
		catch (final IllegalBlockSizeException e) {
		}
		catch (final IllegalArgumentException e) {
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
			
			byte[] fullData = new byte[mSalt.length + currentIV.length + enc.length];
			int offset = 0;
			System.arraycopy(mSalt, 0, fullData, offset, mSalt.length);
			offset += mSalt.length;
			System.arraycopy(currentIV, 0, fullData, offset, currentIV.length);
			offset += currentIV.length;
			System.arraycopy(enc, 0, fullData, offset, enc.length);
			
		String base64Data = DesEncrypter.toBase64(fullData);
		return EncryptionHeader.PREFIX_AES256 + base64Data;
		}
		catch (final BadPaddingException e) {
			LogUtils.severe("AES-256 Encryption failed: bad padding", e);
		}
		catch (final IllegalBlockSizeException e) {
			LogUtils.severe("AES-256 Encryption failed: illegal block size", e);
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
					final GCMParameterSpec gcmSpec = new GCMParameterSpec(128, currentIV);
					ecipher = Cipher.getInstance(CIPHER_ALGORITHM);
					ecipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
				} else {
					currentIV = iv;
					final GCMParameterSpec gcmSpec = new GCMParameterSpec(128, currentIV);
					dcipher = Cipher.getInstance(CIPHER_ALGORITHM);
					dcipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
				}
			}
			catch (final InvalidAlgorithmParameterException e) {
				LogUtils.severe(e);
			}
			catch (final InvalidKeySpecException e) {
				LogUtils.severe(e);
			}
			catch (final NoSuchPaddingException e) {
				LogUtils.severe(e);
			}
			catch (final NoSuchAlgorithmException e) {
				LogUtils.severe(e);
			}
			catch (final InvalidKeyException e) {
				LogUtils.severe(e);
			}
		}
	}
	
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

