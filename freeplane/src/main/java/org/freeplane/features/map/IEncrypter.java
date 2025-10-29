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
package org.freeplane.features.map;

/**
 * Interface for encryption/decryption implementations.
 * 
 * Security note: Implementations must properly clean up sensitive data
 * (passwords, keys, salts) when destroy() is called.
 * 
 * @author Dimitry Polivaev
 * 02.01.2009
 */
public interface IEncrypter {
	/**
	 * Decrypt an encrypted string.
	 * @param str the encrypted string
	 * @return the decrypted content, or null if decryption fails
	 */
	public String decrypt(String str);

	/**
	 * Encrypt a string.
	 * @param str the plaintext string
	 * @return the encrypted content, or null if encryption fails
	 */
	public String encrypt(final String str);
	
	/**
	 * Clean up sensitive data from memory.
	 * This method should:
	 * - Zero out password arrays
	 * - Clear salt arrays
	 * - Null cipher references
	 * 
	 * Call this method when the encrypter is no longer needed to prevent
	 * passwords from remaining in memory longer than necessary.
	 */
	public void destroy();
}
