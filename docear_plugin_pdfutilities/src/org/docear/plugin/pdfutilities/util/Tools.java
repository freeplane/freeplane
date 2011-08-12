package org.docear.plugin.pdfutilities.util;

import java.io.File;
import java.io.IOException;
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
	
	//TODO: getFilefromUri and getAbsoluteUri currently are working for current Map only !!! 
	
	//TODO: check if URI refers to a local file !!
	
	public static File getFilefromUri(URI uri){		
		if(uri == null) return null;
		try {
			return new File(uri.normalize());
		} 
		catch (IllegalArgumentException e) {
			return new File(getAbsoluteUri(uri));
		}
	}
	
	public static URI getAbsoluteUri(URI uri){
		if(uri == null /*|| !isFile(uri)*/) return null;
		try{
			if(!uri.isAbsolute()){
				final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);
				MapModel map = Controller.getCurrentController().getMap();
				if(map == null || urlManager == null) return null;
				uri = urlManager.getAbsoluteUri(map, uri);				
			}
			if(uri.getScheme().equals("file")) return uri;
			return uri.toURL().openConnection().getURL().toURI();
		} 
		catch(IllegalArgumentException e){
			return null;
		} 
		catch (MalformedURLException e) {
			return null;
		}
		catch (URISyntaxException e) {
			return null;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	
    public static boolean isFile(URI uri) {
    	final String scheme = uri.getScheme();
		return scheme != null && scheme.equalsIgnoreCase("file");
    }

    public static boolean hasHost(URI uri) {
        String host = uri.getHost();
        return host != null && !"".equals(host);
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
