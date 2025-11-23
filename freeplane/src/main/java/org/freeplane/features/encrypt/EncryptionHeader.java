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

public class EncryptionHeader {
	public static final String PREFIX_AES256 = "FP-AES256-V1:";
	
	public static String stripPrefix(String encryptedString) {
		if (encryptedString == null) {
			return null;
		}
		if (encryptedString.startsWith(PREFIX_AES256)) {
			return encryptedString.substring(PREFIX_AES256.length());
		}
		return null;
	}
}
