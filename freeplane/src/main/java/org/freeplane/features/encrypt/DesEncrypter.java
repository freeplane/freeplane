/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
import java.net.URLEncoder;
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
 * @author Dimitry Polivaev
 * 29.12.2008
 */
public class DesEncrypter implements IEncrypter {
	private static final int SALT_LENGTH = 8;
	private static final String SALT_PRESENT_INDICATOR = " ";

	/**
	 * @throws IOException
	 */
	public static byte[] fromBase64(final String base64String) {
		return Base64Coding.decode64(base64String);
	}

	/**
	 */
	public static String toBase64(final byte[] byteBuffer) {
		return Base64Coding.encode64(byteBuffer);
	}

	private Cipher dcipher;
	private Cipher ecipher;
	int iterationCount = 19;
	final private String mAlgorithm;
	byte[] mSalt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3,
	        (byte) 0x03 };
	private char[] passPhrase;

	public DesEncrypter(final StringBuilder pPassPhrase, final String pAlgorithm) {
		passPhrase = new char[pPassPhrase.length()];
		pPassPhrase.getChars(0, passPhrase.length, passPhrase, 0);
		mAlgorithm = pAlgorithm;
	}

	public String decrypt(String str) {
		if (str == null) {
			return null;
		}
		try {
			byte[] salt = null;
			final int indexOfSaltIndicator = str.indexOf(DesEncrypter.SALT_PRESENT_INDICATOR);
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
			return new String(utf8, "UTF8");
		}
		catch (final javax.crypto.BadPaddingException e) {
		}
		catch (final IllegalBlockSizeException e) {
		}
		catch (final UnsupportedEncodingException e) {
		}
		catch (final IllegalArgumentException e) {
		}
		return null;
	}

	public String encrypt(final String str) {
		try {
			initWithNewSalt();
			if(ecipher == null)
				return null;
			final byte[] utf8 = str.getBytes("UTF8");
			final byte[] enc = ecipher.doFinal(utf8);
			return DesEncrypter.toBase64(mSalt) + DesEncrypter.SALT_PRESENT_INDICATOR + DesEncrypter.toBase64(enc);
		}
		catch (final javax.crypto.BadPaddingException e) {
		}
		catch (final IllegalBlockSizeException e) {
		}
		catch (final UnsupportedEncodingException e) {
		}
		return null;
	}

	public void initWithNewSalt() {
	    final byte[] newSalt = new byte[DesEncrypter.SALT_LENGTH];
	    for (int i = 0; i < newSalt.length; i++) {
	    	newSalt[i] = (byte) (Math.random() * 256l - 128l);
	    }
	    init(newSalt);
    }

	/**
	 */
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
				SecretKey key;
				try{
					KeySpec keySpec = new PBEKeySpec(passPhrase, mSalt, iterationCount);
					key = SecretKeyFactory.getInstance(mAlgorithm).generateSecret(keySpec);
				}
				catch (final java.security.spec.InvalidKeySpecException e) {
					try {
	                    passPhrase = URLEncoder.encode(new String(passPhrase),  "UTF-8").toCharArray();
                    }
                    catch (UnsupportedEncodingException e1) {
                    	throw e;
                    }
					KeySpec keySpec = new PBEKeySpec(passPhrase, mSalt, iterationCount);
					key = SecretKeyFactory.getInstance(mAlgorithm).generateSecret(keySpec);
				}
				ecipher = Cipher.getInstance(mAlgorithm);
				dcipher = Cipher.getInstance(mAlgorithm);
				final AlgorithmParameterSpec paramSpec = new PBEParameterSpec(mSalt, iterationCount);
				ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
				dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			}
			catch (final java.security.InvalidAlgorithmParameterException e) {
				LogUtils.severe(e);
			}
			catch (final java.security.spec.InvalidKeySpecException e) {
				LogUtils.severe(e);
			}
			catch (final javax.crypto.NoSuchPaddingException e) {
				LogUtils.severe(e);
			}
			catch (final java.security.NoSuchAlgorithmException e) {
				LogUtils.severe(e);
			}
			catch (final java.security.InvalidKeyException e) {
				LogUtils.severe(e);
			}
		}
	}
}
