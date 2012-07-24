package org.docear.plugin.core.util;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class ReplaceLigaturesFilter extends CharSequenceFilter {
	private final LinkedList<Character> charBuffer = new LinkedList<Character>();
	
	public static Map<Character, String> ligaturesMap = new HashMap<Character, String>();
	static {		
		ligaturesMap.put((char) 0x00C6, "AE");
		ligaturesMap.put((char) 0x00E6, "ae");

		ligaturesMap.put((char) 0x0152, "OE");
		ligaturesMap.put((char) 0x0153, "oe");

		ligaturesMap.put((char) 0x0132, "IJ");
		ligaturesMap.put((char) 0x0133, "ij");

		ligaturesMap.put((char) 0x2116, "No");

		ligaturesMap.put((char) 0x017F, "s");

		ligaturesMap.put((char) 0xFB00, "ff");
		ligaturesMap.put((char) 0xFB01, "fi");
		ligaturesMap.put((char) 0xFB02, "fl");
		ligaturesMap.put((char) 0xFB03, "ffi");
		ligaturesMap.put((char) 0xFB04, "ffl");
		ligaturesMap.put((char) 0xFB05, "ft");
		ligaturesMap.put((char) 0xFB06, "st");
		// not standardized
		ligaturesMap.put((char) 0xE707, "ct");
		ligaturesMap.put((char) 0xE708, "ch");
		ligaturesMap.put((char) 0xE709, "ck");
		ligaturesMap.put((char) 0xE70A, "fh");
		ligaturesMap.put((char) 0xE70B, "fj");
		ligaturesMap.put((char) 0xE70C, "ft");
		ligaturesMap.put((char) 0xE70D, "ll");
		ligaturesMap.put((char) 0xE70E, "tt");
		ligaturesMap.put((char) 0xE70F, "tz");

		ligaturesMap.put((char) 0xE750, "sb");
		ligaturesMap.put((char) 0xE751, "sch");
		ligaturesMap.put((char) 0xE752, "sh");
		ligaturesMap.put((char) 0xE753, "si");
		ligaturesMap.put((char) 0xE754, "sk");
		ligaturesMap.put((char) 0xE755, "sl");
		ligaturesMap.put((char) 0xE756, "ss");
		ligaturesMap.put((char) 0xE757, "ssi");
		ligaturesMap.put((char) 0xE758, "ssl");
		ligaturesMap.put((char) 0xE759, "ssb");
		ligaturesMap.put((char) 0xE75A, "ssh");
		ligaturesMap.put((char) 0xE75B, "ssk");
		ligaturesMap.put((char) 0xE75C, "sst");

		ligaturesMap.put((char) 0xE770, "fb");
		ligaturesMap.put((char) 0xE771, "ffb");
		ligaturesMap.put((char) 0xE772, "ffh");
		ligaturesMap.put((char) 0xE773, "ffj");
		ligaturesMap.put((char) 0xE774, "fk");
		ligaturesMap.put((char) 0xE775, "ffk");
		ligaturesMap.put((char) 0xE776, "fft");
	}

	public ReplaceLigaturesFilter() {
		this(null);
	}
	public ReplaceLigaturesFilter(CharSequenceFilter filter) {
		super(filter);
	}
	
	protected Character next() throws EOFException, IOException {
		if(charBuffer.size() > 0) {
			return charBuffer.remove();
		}
		else {
			return super.next();
		}					
	}
	
	public Character filterCharacter(Character chr) {			
		if (ligaturesMap.containsKey(chr)) {
			String replacement = ligaturesMap.get(chr);
			for(int i = 1;  i < replacement.length(); i++) {
				charBuffer.add(replacement.charAt(i));
			}
			return replacement.charAt(0);
		} 			
		return chr;
	}
}