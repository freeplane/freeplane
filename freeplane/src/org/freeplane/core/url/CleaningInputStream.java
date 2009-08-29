/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.core.url;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * @author Dimitry Polivaev
 * 27.08.2009
 */
public class CleaningInputStream extends InputStream {
	private static final int MAX_PUSHED_BACK = 4;
	private PushbackInputStream in;
	private int pushedBack = 0; 
	public CleaningInputStream(InputStream in) {
	    super();
	    this.in=new PushbackInputStream(in, MAX_PUSHED_BACK);
    }

	@Override
    public int read() throws IOException {
		byte b = (byte)in.read();
		if(pushedBack > 0){
			pushedBack--;
			return b;
		}
		if(b == '&'){
			byte[] bytes = new byte[MAX_PUSHED_BACK];
			int i = 0; 
			bytes[i++] = b = (byte) in.read();
			if(b == '#'){
				bytes[i++] = b = (byte) in.read();
				if(b == 'x'|| b >= '0' || b <= '3'){
					bytes[i++] = (byte) in.read();
					bytes[i++] = (byte) in.read();
				}
			}
			if(isValidInput(i, bytes)){
				pushedBack = i;
				in.unread(bytes, 0, i);
				return '&';
			}
			else{
				return ' ';
			}
		}
		return b;
    }

	private boolean isValidInput(int i, byte[] bytes) {
		if(i != MAX_PUSHED_BACK || bytes[i-1] != ';'){
			return true;
		}
		final int  c;
		if(bytes[1] == 'x'){
			 c = Character.digit(bytes[2], 16);
		}
		else{
			c = Character.digit(bytes[1], 10) * 10 + Character.digit(bytes[2], 10);
		}
		return  c >= ' ' || c == '\t' || c == '\r' || c == '\n';
    }

}
