package org.docear.plugin.core.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;

public abstract class CharSequenceFilter {
	private final CharSequenceFilter previousFilter;
	private StringReader reader;
	
	public CharSequenceFilter(CharSequenceFilter filter) {
		this.previousFilter = filter;
	}
	
	protected Character next() throws IOException, EOFException {
		if(previousFilter == null) {
			if(reader == null) {
				throw new IllegalStateException("no String input was set");
			}
			int chr = reader.read();
			if(chr > -1) {
				return (char) chr;
			}
			throw new EOFException();
		}
		else {
			return previousFilter.next(); 
		}
	}
	
	private void propagateText(String text) {
		if(previousFilter == null) {
			reader = new StringReader(text);
		} 
		else {
			previousFilter.propagateText(text);
		}
	}
	
	public final String filter(String text) throws IOException {
		propagateText(text);
		StringBuffer buffer = new StringBuffer();		
		Character chr;
		boolean eof = false;
		while(!eof) {
			try {
				chr = next();
				
				if(chr == null) {
					continue;
				}
				else {
					buffer.append(filterCharacter(chr));
				}
			} 
			catch (EOFException e) {
				eof = true;
			}				
		}
		return buffer.toString();
	}
	
	public abstract Character filterCharacter(Character chr);  
}