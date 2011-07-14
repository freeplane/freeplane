package org.docear.plugin.pdfutilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

public class Tools {
	
	public static File getFilefromUri(URI uri){
		if(uri == null) return null;
		try{
			if(!uri.isAbsolute()){
				final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);
				MapModel map = Controller.getCurrentController().getMap();
				if(map == null || urlManager == null) return null;
				uri = urlManager.getAbsoluteUri(map, uri);				
			}
			return new File(uri);
		} catch(IllegalArgumentException e){
			return null;
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public static List<File> textURIListToFileList(String data) {
	    List<File> list = new ArrayList<File>();
	    StringTokenizer stringTokenizer = new StringTokenizer(data, "\r\n");
	    while(stringTokenizer.hasMoreTokens()) {
	    	String string = stringTokenizer.nextToken();
	    	// the line is a comment (as per the RFC 2483)
	    	if (string.startsWith("#")) continue;
		    		    
			try {
				URI uri = new URI(string);
				File file = new File(uri);
			    list.add(file);
			} catch (URISyntaxException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an URISyntaxException occured. URI: " + string);
			} catch (IllegalArgumentException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an IllegalArgumentException occured. URI: " + string);
		    }	    
	    }	     
	    return list;
	}	

}
