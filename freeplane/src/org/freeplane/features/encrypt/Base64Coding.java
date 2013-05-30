/*
 * AbstractPreferences -- Partial implementation of a Preference node Copyright
 * (C) 2001, 2003, 2004, 2006 Free Software Foundation, Inc. This file is part
 * of GNU Classpath. GNU Classpath is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2, or (at your
 * option) any later version. GNU Classpath is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with GNU Classpath; see the file COPYING. If
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301 USA. Linking this library statically or
 * dynamically with other mapViews is making a combined work based on this
 * library. Thus, the terms and conditions of the GNU General Public License
 * cover the whole combination. As a special exception, the copyright holders of
 * this library give you permission to link this library with independent
 * mapViews to produce an executable, regardless of the license terms of these
 * independent mapViews, and to copy and distribute the resulting executable
 * under terms of your choice, provided that you also meet, for each linked
 * independent mapView, the terms and conditions of the license of that mapView.
 * An independent mapView is a mapView which is not derived from or based on
 * this library. If you modify this library, you may extend this exception to
 * your version of the library, but you are not obligated to do so. If you do
 * not wish to do so, delete this exception statement from your version.
 */
package org.freeplane.features.encrypt;

import java.io.ByteArrayOutputStream;

/**
 * Partial implementation of a Preference node.
 * 
 * @since 1.4
 * @author Mark Wielaard (mark@klomp.org) 22.3.2008: FC: Changed name from
 *         AbstractPreferences to the current, Removed all but base64 coding.
 */
public class Base64Coding {
	/**
	 * Helper method for decoding a Base64 string as an byte array. Returns null
	 * on encoding error. This method does not allow any other characters
	 * present in the string then the 65 special base64 chars.
	 */
	public static byte[] decode64(final String s) {
		final ByteArrayOutputStream bs = new ByteArrayOutputStream((s.length() / 4) * 3);
		final char[] c = new char[s.length()];
		s.getChars(0, s.length(), c, 0);
		int endchar = -1;
		for (int j = 0; j < c.length && endchar == -1; j++) {
			if (c[j] >= 'A' && c[j] <= 'Z') {
				c[j] -= 'A';
			}
			else if (c[j] >= 'a' && c[j] <= 'z') {
				c[j] = (char) (c[j] + 26 - 'a');
			}
			else if (c[j] >= '0' && c[j] <= '9') {
				c[j] = (char) (c[j] + 52 - '0');
			}
			else if (c[j] == '+') {
				c[j] = 62;
			}
			else if (c[j] == '/') {
				c[j] = 63;
			}
			else if (c[j] == '=') {
				endchar = j;
			}
			else {
				return null;
			}
		}
		int remaining = endchar == -1 ? c.length : endchar;
		int i = 0;
		while (remaining > 0) {
			byte b0 = (byte) (c[i] << 2);
			if (remaining >= 2) {
				b0 += (c[i + 1] & 0x30) >> 4;
			}
			bs.write(b0);
			if (remaining >= 3) {
				byte b1 = (byte) ((c[i + 1] & 0x0F) << 4);
				b1 += (byte) ((c[i + 2] & 0x3C) >> 2);
				bs.write(b1);
			}
			if (remaining >= 4) {
				byte b2 = (byte) ((c[i + 2] & 0x03) << 6);
				b2 += c[i + 3];
				bs.write(b2);
			}
			i += 4;
			remaining -= 4;
		}
		return bs.toByteArray();
	}

	/**
	 * Helper method for encoding an array of bytes as a Base64 String.
	 */
	public static String encode64(final byte[] b) {
		final StringBuilder sb = new StringBuilder((b.length / 3) * 4);
		int i = 0;
		int remaining = b.length;
		final char c[] = new char[4];
		while (remaining > 0) {
			c[0] = (char) ((b[i] & 0xFC) >> 2);
			c[1] = (char) ((b[i] & 0x03) << 4);
			if (remaining >= 2) {
				c[1] += (char) ((b[i + 1] & 0xF0) >> 4);
				c[2] = (char) ((b[i + 1] & 0x0F) << 2);
				if (remaining >= 3) {
					c[2] += (char) ((b[i + 2] & 0xC0) >> 6);
					c[3] = (char) (b[i + 2] & 0x3F);
				}
				else {
					c[3] = 64;
				}
			}
			else {
				c[2] = 64;
				c[3] = 64;
			}
			for (int j = 0; j < 4; j++) {
				if (c[j] < 26) {
					c[j] += 'A';
				}
				else if (c[j] < 52) {
					c[j] = (char) (c[j] - 26 + 'a');
				}
				else if (c[j] < 62) {
					c[j] = (char) (c[j] - 52 + '0');
				}
				else if (c[j] == 62) {
					c[j] = '+';
				}
				else if (c[j] == 63) {
					c[j] = '/';
				}
				else {
					c[j] = '=';
				}
			}
			sb.append(c);
			i += 3;
			remaining -= 3;
		}
		return sb.toString();
	}
}
