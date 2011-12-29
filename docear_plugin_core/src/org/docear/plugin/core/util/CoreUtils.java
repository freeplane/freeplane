package org.docear.plugin.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.UrlManager;

public class CoreUtils {
	public static File resolveURI(final URI uri) {
		if(uri == null) {
			return null;
		}
		try {
			if(uri.getFragment() != null) {
				return null;
			}
			URI absoluteUri = absoluteURI(uri);
			if (absoluteUri == null) {
				return null;
			}
			if("file".equalsIgnoreCase(absoluteUri.getScheme())){
				return new File(absoluteUri);
			}
		}
		catch(Exception ex) {
			LogUtils.warn(ex);
		}		
		return null;
	}
	
	public static URI absoluteURI(final URI uri) {
		return absoluteURI(uri, null);

	}
	
	public static URI absoluteURI(final URI uri, MapModel map) {
		if(uri == null) {
			return null;
		}
		try {
			URLConnection urlConnection;
			// windows drive letters are interpreted as uri schemes -> make a file from the scheme-less uri string and use this to resolve the path
			if(Compat.isWindowsOS() && (uri.getScheme() != null && uri.getScheme().length() == 1)) { 
				urlConnection = (new File(uri.toString())).toURL().openConnection();
			} 
			else if(uri.getScheme() == null && !uri.getPath().startsWith(File.separator)) {
				if(map != null) {
					urlConnection = (new File(uri.toString())).toURL().openConnection();
				} 
				else {
					urlConnection = UrlManager.getController().getAbsoluteUri(map, uri).toURL().openConnection();
				}
			}
			else {
				urlConnection = uri.toURL().openConnection();				
			}
			
			if (urlConnection == null) {
				return null;
			}
			else {
				return urlConnection.getURL().toURI().normalize();
			}
		}
		catch (URISyntaxException e) {
			LogUtils.warn(e);
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}
		catch (Exception e){
			LogUtils.warn(e);
		}
		return uri.normalize();

	}
}
