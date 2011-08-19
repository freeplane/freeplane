package org.docear.plugin.pdfutilities.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

public class Tools {
	
	//TODO: check if URI refers to a local file !!
	
	public static File getFilefromUri(URI uri){		
		if(uri == null) return null;
		try {
			return new File(uri.normalize());
		} 
		catch (IllegalArgumentException e) {
			//return new File(getAbsoluteUri(uri, map));
			LogUtils.severe("(getFilefromUri) Uri has to be absolute: " + uri);
			return null;
		}
	}
	
	public static URI getAbsoluteUri(NodeModel node){
		URI uri = NodeLinks.getValidLink(node);
		return Tools.getAbsoluteUri(uri, node.getMap());
	}
	
	public static URI getAbsoluteUri(NodeModel node, MapModel map){
		URI uri = NodeLinks.getValidLink(node);
		return Tools.getAbsoluteUri(uri, map);
	}
	
	public static URI getAbsoluteUri(URI uri){
		return Tools.getAbsoluteUri(uri, Controller.getCurrentController().getMap());
	}
	
	public static URI getAbsoluteUri(URI uri, MapModel map){
		if(uri == null) return null;
		try{
			// uri with scheme is always "absolute" ( so are workspace relative links)
			if(!uri.isAbsolute()){
				final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);
				//MapModel map = Controller.getCurrentController().getMap();
				if(map == null || urlManager == null) return null;
				if(uri.getScheme() == null ||uri.getScheme().equals(""))
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

	public static String reshapeString(String s, int i) {
		s = s.trim();
		if(s.length() > i){
			s = s.substring(0, i - 4);
			s = s + "...";
		}
		return s;
	}
	
	public static boolean exists(URI uri) {
		return Tools.exists(uri, Controller.getCurrentController().getMap());
	}

	public static boolean exists(URI uri, MapModel map) {
		uri = Tools.getAbsoluteUri(uri, map);
		try {
			if(uri.toURL().openConnection().getContentLength() > 0) {
				return true;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static Collection<URI> getFilteredFileList(URI absoluteURI, FileFilter fileFilter, boolean readSubDirectories) {
		Collection<URI> result = new ArrayList<URI>();
		Collection<File> tempResult = new ArrayList<File>();
		if(!absoluteURI.isAbsolute()) return result;
		
		File monitoringDir = Tools.getFilefromUri(absoluteURI);
		File[] monitorFiles = monitoringDir.listFiles(fileFilter);
		if(monitorFiles != null && monitorFiles.length > 0){
			tempResult.addAll(Arrays.asList(monitorFiles));
		}	
		for(File file : tempResult){
			result.add(file.toURI());
		}
		if(readSubDirectories){
			File[] subDirs = monitoringDir.listFiles(new DirectoryFileFilter());
			if(subDirs != null && subDirs.length > 0){
				for(File subDir : subDirs){
					result.addAll(Tools.getFilteredFileList(subDir.toURI(), fileFilter, readSubDirectories));
				}
			}			
		}		
		return result;
	}
		

}
