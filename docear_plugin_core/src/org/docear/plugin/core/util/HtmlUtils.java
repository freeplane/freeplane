package org.docear.plugin.core.util;

import java.io.IOException;
import java.io.StringReader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.freeplane.core.util.LogUtils;

public class HtmlUtils {
	
	public static String extractText(String text) {
	    final StringBuilder stringBuilder = new StringBuilder();
	    stringBuilder.append("");
	    
	    ParserDelegator parserDelegator = new ParserDelegator();
	    
	    ParserCallback parserCallback = new ParserCallback() {
	      
	    	public void handleText(final char[] data, final int pos) {
	    		stringBuilder.append(data);
	    	}
	      
	    	public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) { }
	      
	    	public void handleEndTag(Tag t, final int pos) {  }
	      
	    	public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) { }
	      
	    	public void handleComment(final char[] data, final int pos) { }
	      
	    	public void handleError(final java.lang.String errMsg, final int pos) { }
	    };
	    
	    try {
			parserDelegator.parse(new StringReader(text), parserCallback, true);
		} catch (IOException e) {
			LogUtils.warn(e);
		}
	    
	    return stringBuilder.toString();
	}
}
