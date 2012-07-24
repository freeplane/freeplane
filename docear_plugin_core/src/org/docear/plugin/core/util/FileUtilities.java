package org.docear.plugin.core.util;

import java.io.IOException;

public class FileUtilities {
	

	public static String replaceLigatures(String text) {
		CharSequenceFilter filter = new ReplaceLigaturesFilter();
		try {
			return filter.filter(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static String getCleanFileName(String path) {
		CharSequenceFilter filter = new ReplaceLigaturesFilter();
		
		filter = new GeneralPunctuationFilter(filter);
		
		filter = new DocearFileNameFilter(filter);
		
		try {
			return filter.filter(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
