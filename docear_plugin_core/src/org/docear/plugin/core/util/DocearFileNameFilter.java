package org.docear.plugin.core.util;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class DocearFileNameFilter extends CharSequenceFilter {
	private final LinkedList<Character> charBuffer = new LinkedList<Character>();

	private static Map<Character, String> fileNameFilter = new HashMap<Character, String>();
	static {
		fileNameFilter.put('|'," ");
		fileNameFilter.put('?'," ");
		fileNameFilter.put('*'," ");
		fileNameFilter.put('\\'," ");
		fileNameFilter.put('/'," ");
		fileNameFilter.put('\''," ");
		fileNameFilter.put(':', "--");
		fileNameFilter.put('"'," ");
	}
	
	public DocearFileNameFilter() {
		this(null);
	}
	public DocearFileNameFilter(CharSequenceFilter filter) {
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
		if (fileNameFilter.containsKey(chr)) {
			String replacement = fileNameFilter.get(chr);
			for(int i = 1;  i < replacement.length(); i++) {
				charBuffer.add(replacement.charAt(i));
			}
			return replacement.charAt(0);
		} 			
		return chr;
	}
}