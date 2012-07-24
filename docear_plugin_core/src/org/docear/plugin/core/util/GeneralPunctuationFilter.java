package org.docear.plugin.core.util;

import java.util.HashMap;
import java.util.Map;


public class GeneralPunctuationFilter extends CharSequenceFilter {
	
	public static Map<Character, Character> generalPunctionsMap = new HashMap<Character, Character>();
	static {
		generalPunctionsMap.put((char) 0x00B6,'\n'); //&para;
		generalPunctionsMap.put((char) 0x00A1,'!');	//&iexcl;
		generalPunctionsMap.put((char) 0x00BF,'?');	//&iquest;
		generalPunctionsMap.put((char) 0x2039,'<');	//&lsaquo;
		generalPunctionsMap.put((char) 0x203A,'>');	//&rsaquo;
		generalPunctionsMap.put((char) 0x00AB,'<');	//&laquo;
		generalPunctionsMap.put((char) 0x00BB,'>');	//&raquo;
		
		generalPunctionsMap.put((char) 0x2018,'\''); //&lsquo;
		generalPunctionsMap.put((char) 0x2019,'\''); //&rsquo;
		generalPunctionsMap.put((char) 0x201A,'\''); //&sbquo;
		generalPunctionsMap.put((char) 0x201C,'"');	//&ldquo;
		generalPunctionsMap.put((char) 0x201D,'"'); //&rdquo;
		generalPunctionsMap.put((char) 0x201E,'"'); //&bdquo;
		generalPunctionsMap.put((char) 0x201F,'"'); 
		
		generalPunctionsMap.put((char) 0x2022,'-'); //&bull;
		
		//dashes and hyphens
		//generalPunctionsMap.put((char) 0x00AD,'-'); //&shy;
		
		generalPunctionsMap.put((char) 0x2010,'-');
		generalPunctionsMap.put((char) 0x2011,'-');
		generalPunctionsMap.put((char) 0x2012,'-');
		generalPunctionsMap.put((char) 0x2013,'-'); //&ndash;
		generalPunctionsMap.put((char) 0x2014,'-'); //&mdash;
		generalPunctionsMap.put((char) 0x2015,'-');
	}
	

	public GeneralPunctuationFilter() {
		this(null);
	}
	public GeneralPunctuationFilter(CharSequenceFilter filter) {
		super(filter);
	}
	
	public Character filterCharacter(Character chr) {			
		if (generalPunctionsMap.containsKey(chr)) {
			return generalPunctionsMap.get(chr);
		} 			
		return chr;
	}
}